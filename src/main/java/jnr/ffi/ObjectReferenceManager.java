package jnr.ffi;

/**
 *
 */
public abstract class ObjectReferenceManager {

    public static ObjectReferenceManager newInstance(Runtime runtime) {
        return runtime.newObjectReferenceManager();
    }

    public abstract Pointer newReference(Object obj);
    public abstract void freeReference(Pointer reference);
    public abstract Object getObject(Pointer reference);
}
