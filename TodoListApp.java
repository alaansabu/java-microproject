import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TodoListApp {
    private static final String URL = "jdbc:mysql://localhost:3306/todo_db?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField taskField, priorityField, dueDateField, descriptionField;
    private JComboBox<String> filterDropdown, categoryDropdown;
    private JLabel totalTasksLabel, completedTasksLabel, filterLabel, categoryLabel;
    private JButton darkModeButton, addButton, deleteButton, completeButton;
    private boolean darkMode = false;

    public TodoListApp() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("To-Do List Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 500);
        frame.setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Task", "Priority", "Due Date", "Description", "Status", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        taskField = new JTextField();
        priorityField = new JTextField();
        dueDateField = new JTextField();
        descriptionField = new JTextField();
        categoryDropdown = new JComboBox<>(new String[]{"Work", "Personal"});

        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityField);
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryDropdown);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Task");
        deleteButton = new JButton("Remove Task");
        completeButton = new JButton("Mark as Done");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(250, frame.getHeight()));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        filterLabel = new JLabel("Filter Tasks:");
        filterDropdown = new JComboBox<>(new String[]{"All Tasks", "Pending", "Completed", "High Priority", "Work", "Personal"});
        filterDropdown.addActionListener(e -> loadTasksFromDatabase());
        sidePanel.add(filterLabel);
        sidePanel.add(filterDropdown);

        totalTasksLabel = new JLabel("Total Tasks: 0");
        completedTasksLabel = new JLabel("Completed: 0");
        sidePanel.add(totalTasksLabel);
        sidePanel.add(completedTasksLabel);

        darkModeButton = new JButton("Toggle Dark Mode");
        darkModeButton.addActionListener(e -> toggleDarkMode());
        sidePanel.add(darkModeButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(sidePanel, BorderLayout.EAST);

        addButton.addActionListener(e -> addTaskToDatabase());
        deleteButton.addActionListener(e -> deleteTaskFromDatabase());
        completeButton.addActionListener(e -> markTaskAsCompleted());

        frame.setVisible(true);
        loadTasksFromDatabase();
    }

    private void addTaskToDatabase() {
        String task = taskField.getText();
        String priority = priorityField.getText();
        String dueDate = dueDateField.getText();
        String description = descriptionField.getText();
        String category = (String) categoryDropdown.getSelectedItem();

        String sql = "INSERT INTO tasks (task, priority, due_date, description, status, category) VALUES (?, ?, ?, ?, 'Pending', ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task);
            pstmt.setString(2, priority);
            pstmt.setString(3, dueDate);
            pstmt.setString(4, description);
            pstmt.setString(5, category);
            pstmt.executeUpdate();

            loadTasksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromDatabase() {
        tableModel.setRowCount(0);
        int totalTasks = 0, completedTasks = 0;
        String selectedFilter = filterDropdown.getSelectedItem().toString();
        String sql = "SELECT * FROM tasks";
        
        if (selectedFilter.equals("Pending")) {
            sql += " WHERE status = 'Pending'";
        } else if (selectedFilter.equals("Completed")) {
            sql += " WHERE status = 'Completed'";
        } else if (selectedFilter.equals("High Priority")) {
            sql += " WHERE priority = 'High'";
        } else if (selectedFilter.equals("Work") || selectedFilter.equals("Personal")) {
            sql += " WHERE category = '" + selectedFilter + "'";
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("task"),
                        rs.getString("priority"),
                        rs.getString("due_date"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("category")
                };
                tableModel.addRow(row);
                totalTasks++;
                if (rs.getString("status").equals("Completed")) completedTasks++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalTasksLabel.setText("Total Tasks: " + totalTasks);
        completedTasksLabel.setText("Completed: " + completedTasks);
    }

    private void deleteTaskFromDatabase() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to delete.");
            return;
        }

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
            loadTasksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markTaskAsCompleted() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to mark as completed.");
            return;
        }

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = "UPDATE tasks SET status = 'Completed' WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
            loadTasksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        if (darkMode) {
            frame.getContentPane().setBackground(Color.DARK_GRAY);
            table.setBackground(Color.DARK_GRAY);
            table.setForeground(Color.WHITE);
        } else {
            frame.getContentPane().setBackground(Color.WHITE);
            table.setBackground(Color.WHITE);
            table.setForeground(Color.BLACK);
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoListApp::new);
    }
}
