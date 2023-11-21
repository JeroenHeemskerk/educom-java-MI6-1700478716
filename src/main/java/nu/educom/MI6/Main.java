package nu.educom.MI6;
import java.util.Scanner;
import java.util.ArrayList;

public class Main
{
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    ArrayList<String> blackList = new ArrayList<>();
    ArrayList<String> loggedInList = new ArrayList<>();

    // Test

    while (true)
    {
      System.out.println("Voer je dienstnummer in:");
      String input = scanner.nextLine();

      if (input.matches("\\d+"))
      {
        if (input.length() == 1 || input.length() == 2)
        {
          input = String.format("%03d", Integer.parseInt(input));
        }
        try
        {
          int number = Integer.parseInt(input);
          System.out.println("Wat is de geheime code?");

          if (number >= 1 && number <= 956 && !blackList.contains(input) && !loggedInList.contains(input))
          {
            String input2 = scanner.nextLine();
            if ("For ThE Royal QUEEN".equals(input2))
            {
              System.out.println("Je bent nu ingelogd agent: " + input + "!");
              loggedInList.add(input);
            }
            else
            {
              System.out.println("Je wordt nu geblacklist");
              blackList.add(input);
            }
          }
          else
          {
            System.out.println("Ongeldig dienstnummer!");
          }
        }
        catch (NumberFormatException e)
        {
          System.out.println("Ongeldig dienstnummer! exception");
        }
      }
    }
  }
}