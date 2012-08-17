package net.vivekiyer.GAL;

import android.view.MenuItem;
import android.widget.PopupMenu;

public class HoneycombListeners {
	
	public static PopupMenu.OnMenuItemClickListener getPopupMenuListener(final android.support.v4.app.Fragment recipient) {
		return new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return recipient.onOptionsItemSelected(item);
			}
		};
	}

}
