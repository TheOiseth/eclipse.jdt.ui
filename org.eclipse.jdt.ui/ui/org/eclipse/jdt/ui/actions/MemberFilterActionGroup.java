/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.actions;

import java.util.ArrayList;

import org.eclipse.swt.custom.BusyIndicator;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.jdt.ui.PreferenceConstants;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.viewsupport.MemberFilter;
import org.eclipse.jdt.internal.ui.viewsupport.MemberFilterAction;

/**
 * Action Group that contributes filter buttons for a view parts showing
 * methods and fields. Contributed filters are: hide fields, hide static
 * members hide non-public members and hide local types.
 * <p>
 * The action group installs a filter on a structured viewer. The filter is connected
 * to the actions installed in the view part's toolbar menu and is updated when the
 * state of the buttons changes.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MemberFilterActionGroup extends ActionGroup {

	public static final int FILTER_NONPUBLIC= MemberFilter.FILTER_NONPUBLIC;
	public static final int FILTER_STATIC= MemberFilter.FILTER_STATIC;
	public static final int FILTER_FIELDS= MemberFilter.FILTER_FIELDS;

	/** @since 3.0 */
	public static final int FILTER_LOCALTYPES= MemberFilter.FILTER_LOCALTYPES;
	/** @since 3.0 */
	public static final int ALL_FILTERS= FILTER_NONPUBLIC | FILTER_FIELDS | FILTER_STATIC | FILTER_LOCALTYPES;

	private static final String TAG_HIDEFIELDS= "hidefields"; //$NON-NLS-1$
	private static final String TAG_HIDESTATIC= "hidestatic"; //$NON-NLS-1$
	private static final String TAG_HIDENONPUBLIC= "hidenonpublic"; //$NON-NLS-1$
	private static final String TAG_HIDELOCALTYPES= "hidelocaltypes"; //$NON-NLS-1$

	private MemberFilterAction[] fFilterActions;
	private MemberFilter fFilter;

	private StructuredViewer fViewer;
	private String fViewerId;
	private boolean fInViewMenu;


	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 *
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store
	 * the last used filter settings in the preference store
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId) {
		this(viewer, viewerId, false);
	}

	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 *
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store
	 * the last used filter settings in the preference store
	 * @param inViewMenu if <code>true</code> the actions are added to the view
	 * menu. If <code>false</code> they are added to the toolbar.
	 *
	 * @since 2.1
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId, boolean inViewMenu) {
		this(viewer, viewerId, inViewMenu, ALL_FILTERS);
	}

	/**
	 * Creates a new <code>MemberFilterActionGroup</code>.
	 *
	 * @param viewer the viewer to be filtered
	 * @param viewerId a unique id of the viewer. Used as a key to to store
	 * the last used filter settings in the preference store
	 * @param inViewMenu if <code>true</code> the actions are added to the view
	 * menu. If <code>false</code> they are added to the toolbar.
	 * @param availableFilters Specifies which filter action should be contained. <code>FILTER_NONPUBLIC</code>,
	 * <code>FILTER_STATIC</code>, <code>FILTER_FIELDS</code> and <code>FILTER_LOCALTYPES</code>
	 * or a combination of these constants are possible values. Use <code>ALL_FILTERS</code> to select all available filters.
	 *
	 * @since 3.0
	 */
	public MemberFilterActionGroup(StructuredViewer viewer, String viewerId, boolean inViewMenu, int availableFilters) {

		fViewer= viewer;
		fViewerId= viewerId;
		fInViewMenu= inViewMenu;

		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		fFilter= new MemberFilter();

		String title, helpContext;
		ArrayList<MemberFilterAction> actions= new ArrayList<>(4);

		// fields
		int filterProperty= FILTER_FIELDS;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_fields_label;
			helpContext= IJavaHelpContextIds.FILTER_FIELDS_ACTION;
			MemberFilterAction hideFields= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideFields.setDescription(ActionMessages.MemberFilterActionGroup_hide_fields_description);
			hideFields.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_fields_tooltip);
			JavaPluginImages.setLocalImageDescriptors(hideFields, "fields_co.svg"); //$NON-NLS-1$
			actions.add(hideFields);
		}

		// static
		filterProperty= FILTER_STATIC;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_static_label;
			helpContext= IJavaHelpContextIds.FILTER_STATIC_ACTION;
			MemberFilterAction hideStatic= new MemberFilterAction(this, title, FILTER_STATIC, helpContext, filterEnabled);
			hideStatic.setDescription(ActionMessages.MemberFilterActionGroup_hide_static_description);
			hideStatic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_static_tooltip);
			JavaPluginImages.setLocalImageDescriptors(hideStatic, "static_co.svg"); //$NON-NLS-1$
			actions.add(hideStatic);
		}

		// non-public
		filterProperty= FILTER_NONPUBLIC;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_nonpublic_label;
			helpContext= IJavaHelpContextIds.FILTER_PUBLIC_ACTION;
			MemberFilterAction hideNonPublic= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideNonPublic.setDescription(ActionMessages.MemberFilterActionGroup_hide_nonpublic_description);
			hideNonPublic.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_nonpublic_tooltip);
			JavaPluginImages.setLocalImageDescriptors(hideNonPublic, "public_co.svg"); //$NON-NLS-1$
			actions.add(hideNonPublic);
		}

		// local types
		filterProperty= FILTER_LOCALTYPES;
		if (isSet(filterProperty, availableFilters)) {
			boolean filterEnabled= store.getBoolean(getPreferenceKey(filterProperty));
			if (filterEnabled) {
				fFilter.addFilter(filterProperty);
			}
			title= ActionMessages.MemberFilterActionGroup_hide_localtypes_label;
			helpContext= IJavaHelpContextIds.FILTER_LOCALTYPES_ACTION;
			MemberFilterAction hideLocalTypes= new MemberFilterAction(this, title, filterProperty, helpContext, filterEnabled);
			hideLocalTypes.setDescription(ActionMessages.MemberFilterActionGroup_hide_localtypes_description);
			hideLocalTypes.setToolTipText(ActionMessages.MemberFilterActionGroup_hide_localtypes_tooltip);
			JavaPluginImages.setLocalImageDescriptors(hideLocalTypes, "localtypes_co.svg"); //$NON-NLS-1$
			actions.add(hideLocalTypes);
		}

		// order corresponds to order in toolbar
		fFilterActions= actions.toArray(new MemberFilterAction[actions.size()]);

		fViewer.addFilter(fFilter);
	}

	private String getPreferenceKey(int filterProperty) {
		return "MemberFilterActionGroup." + fViewerId + '.' + String.valueOf(filterProperty); //$NON-NLS-1$
	}

	/**
	 * Sets the member filters.
	 *
	 * @param filterProperty the filter to be manipulated. Valid values are <code>FILTER_FIELDS</code>,
	 * <code>FILTER_PUBLIC</code> <code>FILTER_PRIVATE</code> and <code>FILTER_LOCALTYPES_ACTION</code>
	 * as defined by this action group
	 * @param set if <code>true</code> the given filter is installed. If <code>false</code> the
	 * given filter is removed
	 * .
	 */
	public void setMemberFilter(int filterProperty, boolean set) {
		setMemberFilters(new int[] {filterProperty}, new boolean[] {set}, true);
	}

	private void setMemberFilters(int[] propertyKeys, boolean[] propertyValues, boolean refresh) {
		if (propertyKeys.length == 0)
			return;
		Assert.isTrue(propertyKeys.length == propertyValues.length);

		for (int i= 0; i < propertyKeys.length; i++) {
			int filterProperty= propertyKeys[i];
			boolean set= propertyValues[i];

			IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
			boolean found= false;
			for (MemberFilterAction fFilterAction : fFilterActions) {
				int currProperty= fFilterAction.getFilterProperty();
				if (currProperty == filterProperty) {
					fFilterAction.setChecked(set);
					found= true;
					store.setValue(getPreferenceKey(filterProperty), set);
				}
			}
			if (found) {
				if (set) {
					fFilter.addFilter(filterProperty);
				} else {
					fFilter.removeFilter(filterProperty);
				}
			}
		}
		if (refresh) {
			fViewer.getControl().setRedraw(false);
			BusyIndicator.showWhile(fViewer.getControl().getDisplay(), () -> fViewer.refresh());
			fViewer.getControl().setRedraw(true);
		}
	}

	private boolean isSet(int flag, int set) {
		return (flag & set) != 0;
	}

	/**
	 * Returns <code>true</code> if the given filter is installed.
	 *
	 * @param filterProperty the filter to be tested. Valid values are <code>FILTER_FIELDS</code>,
	 * <code>FILTER_PUBLIC</code>, <code>FILTER_PRIVATE</code> and <code>FILTER_LOCALTYPES</code> as defined by this action
	 * group
	 * @return returns <code>true</code> if the given filter is installed
	 */
	public boolean hasMemberFilter(int filterProperty) {
		return fFilter.hasFilter(filterProperty);
	}

	/**
	 * Saves the state of the filter actions in a memento.
	 *
	 * @param memento the memento to which the state is saved
	 */
	public void saveState(IMemento memento) {
		memento.putString(TAG_HIDEFIELDS, String.valueOf(hasMemberFilter(FILTER_FIELDS)));
		memento.putString(TAG_HIDESTATIC, String.valueOf(hasMemberFilter(FILTER_STATIC)));
		memento.putString(TAG_HIDENONPUBLIC, String.valueOf(hasMemberFilter(FILTER_NONPUBLIC)));
		memento.putString(TAG_HIDELOCALTYPES, String.valueOf(hasMemberFilter(FILTER_LOCALTYPES)));
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * @param memento the memento from which the state is restored
	 */
	public void restoreState(IMemento memento) {
		setMemberFilters(
			new int[] {FILTER_FIELDS, FILTER_STATIC, FILTER_NONPUBLIC, FILTER_LOCALTYPES},
			new boolean[] {
				Boolean.parseBoolean(memento.getString(TAG_HIDEFIELDS)),
				Boolean.parseBoolean(memento.getString(TAG_HIDESTATIC)),
				Boolean.parseBoolean(memento.getString(TAG_HIDENONPUBLIC)),
				Boolean.parseBoolean(memento.getString(TAG_HIDELOCALTYPES))
			}, false);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		contributeToToolBar(actionBars.getToolBarManager());
	}

	/**
	 * Adds the filter actions to the given tool bar
	 *
	 * @param tbm the tool bar to which the actions are added
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		if (fInViewMenu)
			return;
		for (MemberFilterAction fFilterAction : fFilterActions) {
			tbm.add(fFilterAction);
		}
	}

	/**
	 * Adds the filter actions to the given menu manager.
	 *
	 * @param menu the menu manager to which the actions are added
	 * @since 2.1
	 */
	public void contributeToViewMenu(IMenuManager menu) {
		if (!fInViewMenu)
			return;
		final String filters= "filters"; //$NON-NLS-1$
		if (menu.find(filters) != null) {
			for (MemberFilterAction action : fFilterActions) {
				menu.prependToGroup(filters, action);
			}
		} else {
			for (MemberFilterAction action : fFilterActions) {
				menu.add(action);
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
