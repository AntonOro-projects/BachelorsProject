package com.example.g10kandidat2019.graph;

import android.content.Context;
import android.os.AsyncTask;

/**
 * General AsyncTask used for loading purposes. ProgressBar appear with your message during task.
 * Use the inner class to perform actions.
 * new AsyncLoadingTask(Context, AsyncLoadingTask.TaskAction() {
 *     // override methods here
 *}
 */
class AsyncLoadingTask extends AsyncTask<String, Integer, String> {

    public interface TaskAction {
        void onPreExecute();
        void doInBackground();
        void onPostExecute();
    }

    private final LoadingDialog dialog;
    private final TaskAction action;

    /**
     * Creates AsyncLoadingTask that performs the actions in the given TaskAction object
     * @param c Context
     * @param action TaskAction object containing the actions you wish to perform
     */
    public AsyncLoadingTask(Context c, TaskAction action, boolean horizontal) {
        this(c, action, "", horizontal);
    }

    /**
     * Creates AsyncLoadingTask that performs the actions in the given TaskAction object
     * @param c Context
     * @param action TaskAction object containing the actions you wish to perform
     * @param msg String displayed on the loading dialog
     */
    private AsyncLoadingTask(Context c, TaskAction action, String msg, boolean horizontal) {
        this.dialog = new LoadingDialog(c, horizontal);
        this.action = action;
        dialog.setMessage(msg);
        dialog.setCancelable(false);
    }

    /**
     * Sets the message String on the loading dialog
     * @param s message
     */
    public void setLoadingMessage(String s) {
        dialog.setMessage(s);
    }

    @Override
    protected String doInBackground(String... strings) {
        action.doInBackground();
        return null;
    }

    @Override
    protected void onPreExecute() {
        action.onPreExecute();
        dialog.show();
        dialog.setSize();
    }

    @Override
    protected void onPostExecute(String s) {
        dialog.dismiss();
        action.onPostExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
    }
}
