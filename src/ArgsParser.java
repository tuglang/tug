import java.util.ArrayList;
import java.util.List;

public class ArgsParser {
    private List<String> commands;
    private List<String> options;
    private List<String> arguments;

    public ArgsParser() {
        commands = new ArrayList<>();
        options = new ArrayList<>();
        arguments = new ArrayList<>();
    }

    public void parse(String[] args) {
        boolean foundFirstCommand = false;
        boolean foundSecondCommand = false;

        for (String arg : args) {
            if (!arg.startsWith("-")) {
                if (!foundFirstCommand) {
                    commands.add(arg);
                    foundFirstCommand = true;
                } else if (!foundSecondCommand) {
                    commands.add(arg);
                    foundSecondCommand = true;
                } else {
                    arguments.add(arg);
                }
            } else if (arg.startsWith("--") && foundFirstCommand && !foundSecondCommand) {
                options.add(arg);
            } else {
                arguments.add(arg);
            }
        }
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
