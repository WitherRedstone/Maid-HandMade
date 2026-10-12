package com.chinaex123.maid_hand_made;

import com.chinaex123.maid_hand_made.maid.task.TaskCreateHandMade;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;

@LittleMaidExtension
public class LittleMaidCompat implements ILittleMaid {

    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskCreateHandMade());
    }
}
