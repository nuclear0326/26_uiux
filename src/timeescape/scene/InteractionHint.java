package timeescape.scene;

import timeescape.core.GameState;
import timeescape.model.Era;

/** Derived from existing progress; no new save fields or interaction rules. */
public enum InteractionHint {
    NONE, LATER, READY;

    public static InteractionHint of(GameState state, String object) {
        boolean powered = state.era()!=Era.PRESENT || state.powered() || object.equals("POWER");
        var item=state.catalog().items().get(object);
        if(item!=null && item.room().equals(state.roomId())) {
            if(state.has(object) || (!item.blockedBy().isBlank() && state.done(item.blockedBy()))) return NONE;
            return powered && item.requires().stream().allMatch(state::done) ? READY : LATER;
        }
        var next=state.nextObjectStep(object);
        if(next.isEmpty()) return NONE;
        var step=next.get();
        return powered && !step.automatic() && state.available(step.id()) ? READY : LATER;
    }
}
