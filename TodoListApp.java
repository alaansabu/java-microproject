import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TodoListApp {
    private DefaultListModel<String> listModel;
    private JList<String> todoList;
    private JTextField taskField;

    public TodoListApp() {
        JFrame frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.getContentPane().setBackground(new Color(240, 248, 255));

        listModel = new DefaultListModel<>();
        todoList = new JList<>(listModel);
        todoList.setBackground(new Color(224, 255, 255));
        todoList.setSelectionBackground(new Color(173, 216, 230));
        todoList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(todoList);

        taskField = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Task");
        JButton completeButton = new JButton("Mark as Done");
        
        addButton.setBackground(new Color(144, 238, 144));
        removeButton.setBackground(new Color(255, 99, 71));
        completeButton.setBackground(new Color(255, 215, 0));
        
        addButton.setForeground(Color.WHITE);
        removeButton.setForeground(Color.WHITE);
        completeButton.setForeground(Color.BLACK);
        
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String task = taskField.getText().trim();
                if (!task.isEmpty()) {
                    listModel.addElement(task);
                    taskField.setText("");
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1) {
                    listModel.remove(selectedIndex);
                }
            }
        });

        completeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String task = listModel.get(selectedIndex);
                    listModel.set(selectedIndex, "✔ " + task);
                }
            }
        });
        
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 248, 255));
        panel.add(taskField);
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(completeButton);
        
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoListApp::new);
    }
}
