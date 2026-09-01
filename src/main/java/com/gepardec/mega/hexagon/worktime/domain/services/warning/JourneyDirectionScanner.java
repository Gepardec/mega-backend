package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

/**
 * Scans a chronologically ordered sequence of journey bookings and reports the warning, if any,
 * for each booking as it is consumed. Single-use and not thread-safe: create one instance per
 * sequence and call {@link #advance} once per booking, in order.
 */
class JourneyDirectionScanner {

    private enum JourneyState {
        NONE, OPEN, CLOSED
    }

    private JourneyState state = JourneyState.NONE;

    WorkTimeWarningType advance(JourneyDirection current, JourneyDirection next) {
        boolean wasOpen = state == JourneyState.OPEN;
        state = transition(current);

        if (current == JourneyDirection.TO) {
            if (wasOpen) {
                return WorkTimeWarningType.BACK_MISSING;
            }
        } else if (!wasOpen) {
            return WorkTimeWarningType.TO_MISSING;
        }

        if (state == JourneyState.OPEN && (next == null || next == JourneyDirection.TO)) {
            state = JourneyState.NONE;
            return WorkTimeWarningType.BACK_MISSING;
        }
        return null;
    }

    private JourneyState transition(JourneyDirection current) {
        if (current == JourneyDirection.TO) {
            return JourneyState.OPEN;
        }
        if (state == JourneyState.NONE) {
            return JourneyState.NONE;
        }
        return current == JourneyDirection.BACK ? JourneyState.CLOSED : JourneyState.OPEN;
    }
}
