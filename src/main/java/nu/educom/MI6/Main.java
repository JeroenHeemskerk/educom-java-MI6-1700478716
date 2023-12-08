package nu.educom.MI6;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Main
{
  public static void main(String[] args)
  {
    Database.openConnection();
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        MI6Application app = new MI6Application();
        app.createAndShowGUI();
      }
    });
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        Database.closeConnection();
      }
    });
  }

}

class MI6Application
{
  private JFrame frame;
  private JTextField serviceNumberField, secretCodeField;
  private JLabel statusLabel;
  public HashMap<String, Long[]> blackList = new HashMap<>();
  public HashMap<String, Long[]> databaseBlackList = new HashMap<>();
  private static final long INITIAL_BLACKLIST_DURATION = 60000;
  private ArrayList<String> loggedInList = new ArrayList<>();
  private JTextArea blacklistStatusArea;
  private Database db = new Database();
  LoginAttempt loginAttempt = new LoginAttempt();

  public void createAndShowGUI()
  {
    frame = new JFrame("MI6 Login System");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    blacklistStatusArea = new JTextArea(5, 50);
    blacklistStatusArea.setEditable(false);
    JScrollPane blacklistScrollPane = new JScrollPane(blacklistStatusArea);
    serviceNumberField = new JTextField(50);
    PlainDocument doc = (PlainDocument) serviceNumberField.getDocument();
    doc.setDocumentFilter(new NumericDocumentFilter());
    secretCodeField = new JTextField(50);
    JButton loginButton = new JButton("Login");
    statusLabel = new JLabel("Voer je dienstnummer in");

    loginAttempt.updateDatabaseBlacklist();
    loginAttempt.loadBlacklistFromDatabase(databaseBlackList);
    generateDatabaseBlackListStatus();
    blacklistStatusArea.setText(generateDatabaseBlackListStatus());
    blackList.putAll(databaseBlackList);

    ActionListener loginListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        handleLogin();
      }
    };

    loginButton.addActionListener(loginListener);
    serviceNumberField.addActionListener(loginListener);
    secretCodeField.addActionListener(loginListener);

    panel.add(new JLabel("Blacklist status:"));
    panel.add(blacklistScrollPane);
    panel.add(statusLabel);
    panel.add(new JLabel("Dienstnummer:"));
    panel.add(serviceNumberField);
    panel.add(new JLabel("Geheime Code:"));
    panel.add(secretCodeField);
    panel.add(loginButton);

    frame.add(panel);

    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        loginAttempt.saveBlacklistToDatabase(blackList);
      }
    });
  }

  private void handleLogin()
  {
    String serviceNumberStr = serviceNumberField.getText();
    String secretCode = secretCodeField.getText();

    if(serviceNumberStr.matches("\\d+"))
    {
      serviceNumberStr = String.format("%03d", Integer.parseInt(serviceNumberStr));
      int serviceNumber = Integer.parseInt(serviceNumberStr);

      Agent agent = new Agent();
      Long[] blacklistDetails = blackList.get(serviceNumberStr);
      boolean isBlacklisted = blacklistDetails != null && System.currentTimeMillis() - blacklistDetails[0] < blacklistDetails[1];

      if(isBlacklisted)
      {
        blacklistDetails[1] = 2 * blacklistDetails[1];
        statusLabel.setText("Agent " + serviceNumberStr + " is momenteel geblacklist, tijd wordt verdubbeld.");
        blackList.put(serviceNumberStr, blacklistDetails);
        generateBlackListStatus();
        blacklistStatusArea.setText(generateBlackListStatus());
      }
      else
      {
        blackList.remove(serviceNumberStr);

        Agent.AgentAuthResult result = agent.authenticateAgent(serviceNumber,secretCode);

        if(result.isAuthenticated)
        {
          statusLabel.setText("Login successvol voor agent: " + serviceNumberStr);
          showAgentDetailsWindow(serviceNumberStr, result.licenceToKill, result.expirationDate);
          Timestamp currentLoginTimestamp = new Timestamp(System.currentTimeMillis());
          loginAttempt.insertLoginAttempt(serviceNumber, true);
          List<String> failedAttempts = loginAttempt.retrieveLoginAttempts(currentLoginTimestamp);
          StringBuilder combinedText = new StringBuilder();
          for (String attempt : failedAttempts)
          {
            combinedText.append(attempt).append("\n");
          }
          combinedText.append(generateBlackListStatus());
          blacklistStatusArea.setText(combinedText.toString());
        }
        else
        {
          statusLabel.setText("Je wordt nu geblacklist (access denied)");
          blackList.put(serviceNumberStr, new Long[]{System.currentTimeMillis(), INITIAL_BLACKLIST_DURATION});
          generateBlackListStatus();
          blacklistStatusArea.setText(generateBlackListStatus());
          loginAttempt.insertLoginAttempt(serviceNumber, false);
        }
      }
    }
    else
    {
      statusLabel.setText("Ongeldig agent nummer (access denied)");
    }
  }

  private String generateBlackListStatus()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    StringBuilder statusBuilder = new StringBuilder();
    for (String servicenumber : blackList.keySet())
    {
      Long[] details = blackList.get(servicenumber);
      long duration = details[1];
      long endTime = details[0] + duration;
      String endTimeFormatted = sdf.format(new Timestamp(endTime));
      long durationInSeconds = duration / 1000;
      statusBuilder.append("Agent ").append(servicenumber)
              .append(" - is geblacklist voor ").append(durationInSeconds)
              .append(" seconden (tot ").append(endTimeFormatted)
              .append(")\n");
    }
    serviceNumberField.setText("");
    secretCodeField.setText("");
    return statusBuilder.toString();
  }

  private String generateDatabaseBlackListStatus()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    StringBuilder statusBuilder = new StringBuilder();
    for (String servicenumber : databaseBlackList.keySet())
    {
      Long[] details = databaseBlackList.get(servicenumber);
      long duration = details[1];
      long endTime = details[0] + duration;
      String endTimeFormatted = sdf.format(new Timestamp(endTime));
      long durationInSeconds = duration / 1000;
      statusBuilder.append("Agent ").append(servicenumber)
              .append(" - is geblacklist voor ").append(durationInSeconds)
              .append(" seconden (tot ").append(endTimeFormatted)
              .append(")\n");
    }
    return statusBuilder.toString();
  }

  private void showAgentDetailsWindow(String serviceNumber, boolean licenceToKill, String expirationDate)
  {
    JFrame detailsFrame = new JFrame("Agent Details");
    detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    detailsFrame.setLayout(new BoxLayout(detailsFrame.getContentPane(), BoxLayout.Y_AXIS));

    detailsFrame.add(new JLabel("Welkom, agent " + serviceNumber));
    detailsFrame.add(new JLabel("Moordbewijs: " + (licenceToKill ? "Ja" : "Nee")));
    detailsFrame.add(new JLabel("Vervaldatum: " + expirationDate));

    detailsFrame.pack();
    Dimension size = detailsFrame.getSize();
    detailsFrame.setSize(new Dimension(size.width * 2, size.height * 2));

    detailsFrame.setLocationRelativeTo(frame);
    Point location = frame.getLocation();
    detailsFrame.setLocation((int) location.getX() + frame.getWidth(), (int) location.getY());

    detailsFrame.setVisible(true);
  }

  private class NumericDocumentFilter extends DocumentFilter
  {
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
    {
      if (string.matches("\\d*"))
      {
        super.insertString(fb, offset, string, attr);
      }
    }

    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
      if (text.matches("\\d*"))
      {
        super.replace(fb, offset, length, text, attrs);
      }
    }
  }

  private void printBlackListContents(String listName, HashMap<String, Long[]> blackListMap) {
    System.out.println("Contents of " + listName + ":");
    for (Map.Entry<String, Long[]> entry : blackListMap.entrySet()) {
      System.out.println("Service Number: " + entry.getKey() + ", Blacklist Details: " +
              Arrays.toString(entry.getValue()));
    }
  }

}
