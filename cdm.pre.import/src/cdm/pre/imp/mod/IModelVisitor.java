package cdm.pre.imp.mod;

import java.lang.reflect.InvocationTargetException;

public interface IModelVisitor {
	public void visit(TreeElement element) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
