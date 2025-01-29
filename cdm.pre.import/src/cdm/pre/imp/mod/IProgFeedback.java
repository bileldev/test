package cdm.pre.imp.mod;

/**
 * Interface needed for the progress indicator.
 * 
 * @author wikeim
 * 
 */
public interface IProgFeedback {
   /**
    * Has to be called when a task is started.
    * 
    * @param name
    *           The name of the task.
    * @param totalWork
    *           The maximum number of work units.
    */
   void beginTask(String name, int totalWork);

   /**
    * Has to be called when the task is finished.
    */
   void done();

   /**
    * Called when a certain number of working units have been finished.
    * 
    * @param work
    *           The number of working units finished.
    */
   void worked(int work);

   /**
    * Called when the task has to be canceled.
    * 
    * @return
    */
   boolean isCanceled();
}
