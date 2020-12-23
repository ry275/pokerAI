import java.util.Scanner;

public class main {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Which game mode would you like to play? ('HumanVsHuman', 'HumanVsAI', 'AIVsAI')");
		String input = scanner.nextLine();
		while (!(input.toUpperCase().equals("HUMANVSHUMAN") || input.toUpperCase().equals("HUMANVSAI") || input.toUpperCase().equals("AIVSAI")))
		{
			System.out.println("Which game mode would you like to play? ('HumanVsHuman', 'HumanVsAI', 'AIVsAI')");
			input = scanner.nextLine();
		}
		
		if (input.toUpperCase().equals("HUMANVSHUMAN")) new Engine(Type.HumanVsHuman).Start();
		else if (input.toUpperCase().equals("HUMANVSAI")) new Engine(Type.HumanVsAI).Start();
		else new Engine(Type.AIVsAI).Start();

		

	}

}
