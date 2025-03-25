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
    private JComboBox<String> filterDropdown;
    private JLabel totalTasksLabel, completedTasksLabel, filterLabel;
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

        String[] columnNames = {"ID", "Task", "Priority", "Due Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        taskField = new JTextField();
        priorityField = new JTextField();
        dueDateField = new JTextField();
        descriptionField = new JTextField();

        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityField);
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);

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
        filterDropdown = new JComboBox<>(new String[]{"All Tasks", "Pending", "Completed", "High Priority"});
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

        String sql = "INSERT INTO tasks (task, priority, due_date, description, status) VALUES (?, ?, ?, ?, 'Pending')";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task);
            pstmt.setString(2, priority);
            pstmt.setString(3, dueDate);
            pstmt.setString(4, description);
            pstmt.executeUpdate();
            loadTasksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        Color bgColor = darkMode ? new Color(45, 45, 45) : new Color(255, 255, 255);
        Color fgColor = darkMode ? Color.white : Color.black;
        Color buttonBgColor = darkMode ? new Color(70, 70, 70) : new Color(220, 220, 220);
        Color buttonFgColor = Color.BLACK; // Ensure text is visible
    
        frame.getContentPane().setBackground(bgColor);
        table.setBackground(bgColor);
        table.setForeground(fgColor);
        
        taskField.setBackground(bgColor);
        taskField.setForeground(fgColor);
        priorityField.setBackground(bgColor);
        priorityField.setForeground(fgColor);
        dueDateField.setBackground(bgColor);
        dueDateField.setForeground(fgColor);
        descriptionField.setBackground(bgColor);
        descriptionField.setForeground(fgColor);
        
        filterDropdown.setBackground(bgColor); // Dark mode for filter dropdown
        filterDropdown.setForeground(fgColor);
        filterLabel.setForeground(fgColor); // Dark mode for filter label
        
        totalTasksLabel.setForeground(fgColor);
        completedTasksLabel.setForeground(fgColor);
    
        darkModeButton.setBackground(buttonBgColor);
        darkModeButton.setForeground(buttonFgColor);
        addButton.setBackground(buttonBgColor);
        addButton.setForeground(buttonFgColor);
        deleteButton.setBackground(buttonBgColor);
        deleteButton.setForeground(buttonFgColor);
        completeButton.setBackground(buttonBgColor);
        completeButton.setForeground(buttonFgColor);
    }
    
       
    
    private void loadTasksFromDatabase() {
        tableModel.setRowCount(0);
        int totalTasks = 0, completedTasks = 0;
        String selectedFilter = filterDropdown.getSelectedItem().toString();
        String sql = "SELECT * FROM tasks";

        if (selectedFilter.equals("Pending")) sql += " WHERE status='Pending'";
        else if (selectedFilter.equals("Completed")) sql += " WHERE status='Completed'";
        else if (selectedFilter.equals("High Priority")) sql += " WHERE priority='High'";

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
                        rs.getString("status")
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoListApp::new);
    }
}