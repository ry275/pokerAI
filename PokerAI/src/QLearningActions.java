import java.util.*;

public class QLearningActions {
	public final List<Action> actions;
	public final String state;
	public QLearningActions(List<Action> actions, String state)
	{
		this.actions = actions;
		this.state = state;
	}
}
