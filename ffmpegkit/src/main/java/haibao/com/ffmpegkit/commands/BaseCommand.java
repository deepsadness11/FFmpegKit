package haibao.com.ffmpegkit.commands;

/**
 * BaseCommand
 * <p>
 * BaseCommmand
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public abstract class BaseCommand implements Command {

    private String command;

    public BaseCommand(String command) {
        this.command = command;
    }

    @Override
    public String getCommand() {
        return command;
    }

    public interface IBuilder {

        Command build();

    }
}
