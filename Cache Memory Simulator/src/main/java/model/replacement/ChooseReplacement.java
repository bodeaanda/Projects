package model.replacement;

import java.util.*;

public class ChooseReplacement {
    public static Optional<ReplacementPolicy> create(String replacement) {
        if(replacement == null)
            return Optional.empty();

        switch (replacement.toUpperCase()) {
            case "LRU" :
                return Optional.of(new LRUReplacement());
            case "FIFO" :
                return Optional.of(new FIFOReplacement());
            case "RANDOM" :
                return Optional.of(new RandomReplacement());
            default :
                return Optional.empty();
        }
    }
}
