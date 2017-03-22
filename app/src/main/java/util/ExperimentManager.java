package util;

/**
 * Created by PCPC on 2017-01-24.
 */

public class ExperimentManager {
    private int block;
    private int trial;

    private boolean done;

    private int maxBlock;
    private int maxTrial;

    public ExperimentManager(int maxBlock, int maxTrial){
        this.maxBlock = maxBlock;
        this.maxTrial = maxTrial;
        trial = 0;
        block  = 0;
        done = false;
    }

    public boolean isDone(){
        return done;
    }

    public boolean addTrial(){
        boolean update = false;
        if(trial == 0){
            update = true;
            trial = 1;
            block = 1;
        }else {
            trial++;
            if (trial > maxTrial) {
                update = true;
                trial = 1;
                block++;
                if (block > maxBlock) {
                    done = true;
                }
            }
        }
        return update;
    }

    public int getBlock(){
        return block;
    }
    public int getTrial(){
        return trial;
    }

}
