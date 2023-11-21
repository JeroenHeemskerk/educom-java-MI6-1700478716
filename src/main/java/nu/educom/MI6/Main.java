package nu.educom.MI6;
import java.util.ArrayList;
import javax.swing.*;

public class Main
{
  public static void main(String[] args)
  {
    ArrayList<String> blackList = new ArrayList<>();
    ArrayList<String> loggedInList = new ArrayList<>();

    while (true)
    {
      String input = showInputDialog("Voer je dienstnummer in:");

      if(input == null)
      {
        break;
      }
      if (input.matches("\\d+"))
      {
        if (input.length() == 1 || input.length() == 2)
        {
          input = String.format("%03d", Integer.parseInt(input));
        }
        try
        {
          int number = Integer.parseInt(input);
          if (number >= 1 && number <= 956 && !blackList.contains(input) && !loggedInList.contains(input)) {
            String input2 = showInputDialog("Wat is de geheime code?");
            if ("For ThE Royal QUEEN".equals(input2)) {
              JOptionPane.showMessageDialog(null, "Je bent nu ingelogd agent: " + input + "!");
              loggedInList.add(input);
            } else {
              JOptionPane.showMessageDialog(null, "Je wordt nu geblacklist (access denied)");
              blackList.add(input);
            }
          } else {
            JOptionPane.showMessageDialog(null, "Ongeldig dienstnummer! (access denied)");
          }
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(null, "Ongeldig dienstnummer! exception");
        }
      }
      else
      {
        JOptionPane.showMessageDialog(null, "Ongeldig dienstnummer! (access denied)");
      }
    }
  }
  private static String showInputDialog(String message)
  {
    return JOptionPane.showInputDialog(null, message);
  }
}