package forms;

import java.util.ArrayList;
import java.util.List;

public class SelectionManager {
    private static final SelectionManager instance = new SelectionManager();
    private List<String[]> selectedBooks = new ArrayList<>();
    private List<SelectionListener> listeners = new ArrayList<>();

    private SelectionManager() {}

    public static SelectionManager getInstance() {
        return instance;
    }

    public void setSelectedBooks(List<String[]> books) {
        this.selectedBooks = books;
        notifyListeners();
    }

    public List<String[]> getSelectedBooks() {
        return selectedBooks;
    }

    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (SelectionListener listener : listeners) {
            listener.onSelectionChanged(selectedBooks);
        }
    }

    public interface SelectionListener {
        void onSelectionChanged(List<String[]> selectedBooks);
    }
}
