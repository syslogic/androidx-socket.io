package io.syslogic.socketio.utils;

import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link RecyclerView} {@link Matcher}.
 * @author Martin Zeitler
 */
public class RecyclerViewMatcher {

    private final int recyclerViewResId;

    public RecyclerViewMatcher(int resId) {
        this.recyclerViewResId = resId;
    }

    private RecyclerView getRecyclerView(@NonNull View view) {
        return view.getRootView().findViewById(recyclerViewResId);
    }

    public Matcher<View> atPosition(final int position) {
        return atPositionOnView(position, -1);
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {
        return new TypeSafeMatcher<>() {
            Resources resources = null;
            View childView;

            @Override
            public boolean matchesSafely(View view) {
                this.resources = view.getResources();
                if (childView == null) {
                    RecyclerView recyclerView = getRecyclerView(view);
                    if (recyclerView == null) {return false;}
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                    if (viewHolder == null) {return false;}
                    childView = viewHolder.itemView;
                }

                if (targetViewId == -1) {
                    return view == childView;
                } else {
                    View targetView = childView.findViewById(targetViewId);
                    return view == targetView;
                }
            }

            @Override
            public void describeTo(Description description) {
                String idDescription = Integer.toString(recyclerViewResId);
                if (this.resources != null) {
                    try {
                        idDescription = this.resources.getResourceName(recyclerViewResId);
                    } catch (Resources.NotFoundException e) {
                        idDescription = String.format("%s (resource name not found)", recyclerViewResId);
                    }
                }
                description.appendText("with id: " + idDescription);
            }
        };
    }
}