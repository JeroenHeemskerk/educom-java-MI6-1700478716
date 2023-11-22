package nu.educom.MI6;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main
{
  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        MI6Application app = new MI6Application();
        app.createAndShowGUI();
      }
    });
  }
}

class MI6Application
{
  private JFrame frame;
  private JTextField serviceNumberField, secretCodeField;
  private JLabel statusLabel;
  private ArrayList<String> blackList = new ArrayList<>();
  private ArrayList<String> loggedInList = new ArrayList<>();

  public void createAndShowGUI()
  {
    frame = new JFrame("MI6 Login System");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    serviceNumberField = new JTextField(10);
    PlainDocument doc = (PlainDocument) serviceNumberField.getDocument();
    doc.setDocumentFilter(new NumericDocumentFilter());
    secretCodeField = new JTextField(30);
    JButton loginButton = new JButton("Login");
    statusLabel = new JLabel("Voer je dienstnummer in");

    ActionListener loginListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        handleLogin();
      }
    };

    // Now add the listener to the buttons and text fields after they have been instantiated
    loginButton.addActionListener(loginListener);
    serviceNumberField.addActionListener(loginListener);
    secretCodeField.addActionListener(loginListener);

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
  }

  private void handleLogin()
  {
    String serviceNumber = serviceNumberField.getText();
    String secretCode = secretCodeField.getText();

    if (serviceNumber.matches("\\d+"))
    {
      if (serviceNumber.length() == 1 || serviceNumber.length() == 2)
      {
        serviceNumber = String.format("%03d", Integer.parseInt(serviceNumber));
      }

      try
      {
        int number = Integer.parseInt(serviceNumber);
        if (number >= 1 && number <= 956 && !blackList.contains(serviceNumber) && !loggedInList.contains(serviceNumber)) {
          if ("For ThE Royal QUEEN".equals(secretCode))
          {
            statusLabel.setText("Je bent nu ingelogd agent: " + serviceNumber);
            loggedInList.add(serviceNumber);
          }
          else
          {
            statusLabel.setText("Je wordt nu geblacklist (access denied)");
            blackList.add(serviceNumber);
          }
        }
        else
        {
          statusLabel.setText("Ongeldig dienstnummer! (access denied)");
        }
      }
      catch (NumberFormatException e)
      {
        statusLabel.setText("Ongeldig dienstnummer! (exception)");
      }
    }
    else
    {
      statusLabel.setText("Ongeldig dienstnummer! (access denied)");
    }
    serviceNumberField.setText("");
    secretCodeField.setText("");
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
}
