package pro.husk.fakeblock.objects;

import com.comphenix.protocol.wrappers.BlockPosition;
import lombok.Getter;

import java.util.HashMap;

public class MultiBlockChangeHandler {

    @Getter
    private final HashMap<BlockPosition, MultiBlockChange> multiBlockChangeHashMap;

    /**
     * Purpose of this class is to provide a working cache for creating MultiBlockChanges
     */
    public MultiBlockChangeHandler() {
        this.multiBlockChangeHashMap = new HashMap<>();
    }

    /**
     * Method to get or create a MultiBlockChange instance given a BlockPosition (location)
     * @param blockPosition location of the change
     * @return MultiBlockChange instance, whether currently existing or new
     */
    public MultiBlockChange getOrCreate(BlockPosition blockPosition) {
        MultiBlockChange multiBlockChange = multiBlockChangeHashMap.get(blockPosition);

        if (multiBlockChange == null) {
            multiBlockChange = new MultiBlockChange();
            multiBlockChangeHashMap.put(blockPosition, multiBlockChange);
        }

        return multiBlockChange;
    }
}