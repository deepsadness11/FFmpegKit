package haibao.com.ffmpegkit.business;

import haibao.com.ffmpegkit.bean.CommandType;

/**
 * 分发任务的接口
 *
 * @author zzx
 * @date 2017/5/8.
 */

public interface CmdDistribution {
    void distributionCommand(CommandType command);

    void onDestroy();

    interface Factory {
        CmdDistribution getDistribution();
    }
}
