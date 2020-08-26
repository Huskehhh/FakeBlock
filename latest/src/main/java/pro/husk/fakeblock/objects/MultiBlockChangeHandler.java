package pro.husk.fakeblock.objects;

import com.comphenix.protocol.wrappers.BlockPosition;
import lombok.Getter;

import java.util.HashMap;

public class MultiBlockChangeHandler {

    @Getter
    private final HashMap<BlockPosition, MultiBlockChange> multiBlockChangeHashMap;

    public MultiBlockChangeHandler() {
        this.multiBlockChangeHashMap = new HashMap<>();
    }

    public MultiBlockChange getOrCreate(BlockPosition blockPosition) {
        MultiBlockChange multiBlockChange = multiBlockChangeHashMap.get(blockPosition);

        if (multiBlockChange == null) {
            multiBlockChange = new MultiBlockChange();
            multiBlockChangeHashMap.put(blockPosition, multiBlockChange);
        }

        return multiBlockChange;
    }
}