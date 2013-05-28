package net.orcades.eclipse.dart.core.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.dart.tools.core.DartCore;
import com.google.dart.tools.core.internal.model.DartProjectNature;
import com.google.dart.tools.core.utilities.resource.IProjectUtilities;

public class ToggleDartNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection iSelection = HandlerUtil.getCurrentSelection(event);

		
		
		if (iSelection instanceof IStructuredSelection) {
			IStructuredSelection iStructuredSelection = (IStructuredSelection) iSelection;
			Object firstElement = iStructuredSelection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) firstElement;
				IProject project = (IProject) adaptable.getAdapter(IProject.class);
				try {
					toggleDart(project);
				} catch (CoreException e) {
					throw new ExecutionException(project.getName()
							+ " dart nature toggle failed", e);
				}

			}

		}

		return null;
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 * @throws CoreException
	 */
	private void toggleDart(IProject project) throws CoreException {

		DartProjectNature dartProjectNature = new DartProjectNature();
		dartProjectNature.setProject(project);
		if (toggleNature(project)) {
			dartProjectNature.configure();
           IProjectUtilities.configurePackagesFilter(project);
		} else {
			dartProjectNature.deconfigure();
			removeResourceFilter(project);
		}

	}

	private boolean toggleNature(IProject project) throws CoreException {

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		for (int i = 0; i < natures.length; ++i) {
			if (DartCore.DART_PROJECT_NATURE.equals(natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length
						- i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);

				removeResourceFilter(project);

				return false;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = DartCore.DART_PROJECT_NATURE;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
		return true;

	}

	private void removeResourceFilter(IProject project) throws CoreException {
		IResourceFilterDescription[] filters = project.getFilters();
		
		
		for (int j = 0; j < filters.length; j++) {
			IResourceFilterDescription filter = filters[j];
			FileInfoMatcherDescription matcherDescription = filter
					.getFileInfoMatcherDescription();
			if ("com.google.dart.tools.core.packagesFolderMatcher"
					.equals(matcherDescription.getId()))
				filter.delete(IResource.BACKGROUND_REFRESH, null);
		}
		
		

	}
}
