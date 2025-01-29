package cdm.pre.imp.mod;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractVisitor implements IModelVisitor {
	protected void visitChildren(TreeElement element) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		for (TreeElement child : element.getChilds()) {
			child.accept(this);
		}
		
	}
}
