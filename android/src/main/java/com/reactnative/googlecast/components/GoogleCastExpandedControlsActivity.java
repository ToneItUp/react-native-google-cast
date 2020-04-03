package com.reactnative.googlecast.components;

import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.google.android.gms.cast.framework.CastButtonFactory;
import android.view.Menu;
import com.reactnative.googlecast.R;


public class GoogleCastExpandedControlsActivity
    extends ExpandedControllerActivity {

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.expanded_controller, menu);
      CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
      return true;
  }

}
