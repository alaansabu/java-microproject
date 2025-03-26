import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class TodoListApp {
    private static final String URL = "jdbc:mysql://localhost:3306/todo_db?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField taskField, priorityField, dueDateField, descriptionField;
    private JComboBox<String> filterDropdown, categoryDropdown;
    private JLabel totalTasksLabel, completedTasksLabel;
    private JButton darkModeButton, addButton, deleteButton, completeButton;
    private boolean darkMode = false;
    private Map<String, Integer> categoryMap = new HashMap<>();

    public TodoListApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeGUI();
        loadCategories(); // Load categories from the database
    }

    private void initializeGUI() {
        frame = new JFrame("To-Do List Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        setupTable();
        setupInputPanel();
        setupButtonPanel();
        setupSidePanel();
        setupEventHandlers();
        frame.setVisible(true);
    }

    private void setupTable() {
        String[] columnNames = {"ID", "Task", "Priority", "Due Date", "Description", "Status", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Task Row
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Task:"), gbc);
        gbc.gridx = 1;
        taskField = new JTextField(25);
        inputPanel.add(taskField, gbc);

        // Priority Row
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        priorityField = new JTextField(10);
        inputPanel.add(priorityField, gbc);

        // Due Date Row
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dueDateField = new JTextField(12);
        inputPanel.add(dueDateField, gbc);

        // Description Row
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(30);
        inputPanel.add(descriptionField, gbc);

        // Category Row
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryDropdown = new JComboBox<>();
        categoryDropdown.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(categoryDropdown, gbc);

        frame.add(inputPanel, BorderLayout.NORTH);
    }

    private void setupButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        addButton = createStyledButton("Add Task", new Color(59, 89, 182));
        deleteButton = createStyledButton("Remove Task", new Color(192, 57, 43));
        completeButton = createStyledButton("Mark as Done", new Color(46, 125, 50));

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }

    private void setupSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(250, frame.getHeight()));
        sidePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        filterDropdown = new JComboBox<>();
        filterDropdown.setMaximumSize(new Dimension(200, 30));
        filterDropdown.addActionListener(e -> loadTasksFromDatabase());

        totalTasksLabel = new JLabel("Total Tasks: 0");
        completedTasksLabel = new JLabel("Completed: 0");
        
        darkModeButton = new JButton("Toggle Dark Mode");
        darkModeButton.addActionListener(e -> toggleDarkMode());

        addSidePanelComponent(sidePanel, new JLabel("Filter Tasks:"));
        addSidePanelComponent(sidePanel, filterDropdown);
        addSidePanelComponent(sidePanel, (JComponent) Box.createVerticalStrut(20));
        addSidePanelComponent(sidePanel, totalTasksLabel);
        addSidePanelComponent(sidePanel, completedTasksLabel);
        addSidePanelComponent(sidePanel, (JComponent) Box.createVerticalGlue());
        addSidePanelComponent(sidePanel, darkModeButton);

        frame.add(sidePanel, BorderLayout.EAST);
    }

    private void addSidePanelComponent(JPanel panel, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comp);
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addTaskToDatabase());
        deleteButton.addActionListener(e -> deleteTaskFromDatabase());
        completeButton.addActionListener(e -> markTaskAsCompleted());
    }

// ... [Keep all imports and class declaration same] ...


    // ... [Keep constant declarations and variables same] ...

    private void loadCategories() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT category_id, category_name FROM categories";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            categoryDropdown.removeAllItems();
            categoryMap.clear();

            while (resultSet.next()) {
                String categoryName = resultSet.getString("category_name");
                int categoryId = resultSet.getInt("category_id");
                categoryDropdown.addItem(categoryName);
                categoryMap.put(categoryName, categoryId);
            }
            updateFilterDropdown();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilterDropdown() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("All Tasks");
        model.addElement("Pending");
        model.addElement("Completed");
        model.addElement("High Priority");
        for (String category : categoryMap.keySet()) {
            model.addElement(category);
        }
        filterDropdown.setModel(model);
    }

    private void loadTasksFromDatabase() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            StringBuilder query = new StringBuilder(
                "SELECT t.id, t.task, t.priority, t.due_date, t.description, t.status, c.category_name " +
                "FROM tasks t " +
                "LEFT JOIN categories c ON t.category_id = c.category_id"
            );

            List<String> conditions = new ArrayList<>();
            List<Object> parameters = new ArrayList<>();

            String selectedFilter = (String) filterDropdown.getSelectedItem();
            if (selectedFilter != null) {
                switch (selectedFilter) {
                    case "Pending":
                        conditions.add("t.status = ?");
                        parameters.add("Pending");
                        break;
                    case "Completed":
                        conditions.add("t.status = ?");
                        parameters.add("Completed");
                        break;
                    case "High Priority":
                        conditions.add("t.priority = ?");
                        parameters.add("High");
                        break;
                    default:
                        if (categoryMap.containsKey(selectedFilter)) {
                            conditions.add("c.category_name = ?");
                            parameters.add(selectedFilter);
                        }
                        break;
                }
            }

            if (!conditions.isEmpty()) {
                query.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query.toString());
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            tableModel.setRowCount(0);
            int totalTasks = 0, completedTasks = 0;
            while (resultSet.next()) {
                Object[] row = {
                    resultSet.getInt("id"),
                    resultSet.getString("task"),
                    resultSet.getString("priority"),
                    resultSet.getDate("due_date"),
                    resultSet.getString("description"),
                    resultSet.getString("status"),
                    resultSet.getString("category_name")
                };
                tableModel.addRow(row);
                
                totalTasks++;
                if ("Completed".equals(resultSet.getString("status"))) {
                    completedTasks++;
                }
            }

            totalTasksLabel.setText("Total Tasks: " + totalTasks);
            completedTasksLabel.setText("Completed: " + completedTasks);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading tasks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTaskToDatabase() {
        if (taskField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Task name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (categoryDropdown.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(frame, "Please select a category!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!dueDateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(frame, "Invalid date format! Use YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoryName = (String) categoryDropdown.getSelectedItem();
        Integer categoryId = categoryMap.get(categoryName);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO tasks (task, priority, due_date, description, status, category_id) " +
                           "VALUES (?, ?, ?, ?, 'Pending', ?)";
            
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, taskField.getText());
            preparedStatement.setString(2, priorityField.getText());
            preparedStatement.setDate(3, Date.valueOf(dueDateField.getText()));
            preparedStatement.setString(4, descriptionField.getText());
            preparedStatement.setInt(5, categoryId);
            
            preparedStatement.executeUpdate();
            
            clearInputFields();
            loadTasksFromDatabase();
            JOptionPane.showMessageDialog(frame, "Task added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clears all input fields in the input panel
    private void clearInputFields() {
        taskField.setText("");
        priorityField.setText("");
        dueDateField.setText("");
        descriptionField.setText("");
        categoryDropdown.setSelectedIndex(-1);
    }

    // Deletes the selected task from the database
    private void deleteTaskFromDatabase() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM tasks WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, taskId);
            preparedStatement.executeUpdate();

            loadTasksFromDatabase();
            JOptionPane.showMessageDialog(frame, "Task deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Marks the selected task as completed in the database
    private void markTaskAsCompleted() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to mark as completed!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE tasks SET status = 'Completed' WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, taskId);
            preparedStatement.executeUpdate();

            loadTasksFromDatabase();
            JOptionPane.showMessageDialog(frame, "Task marked as completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error marking task as completed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ... [Keep other methods same] ...


    private void toggleDarkMode() {
        darkMode = !darkMode;
        Color bgColor = darkMode ? new Color(30, 30, 30) : Color.WHITE;
        Color fgColor = darkMode ? Color.WHITE : Color.BLACK;
    
        frame.getContentPane().setBackground(bgColor);
        table.setBackground(bgColor);
        table.setForeground(fgColor);
        table.setGridColor(darkMode ? Color.GRAY : Color.LIGHT_GRAY);
    
        // Set button text to black in both modes
        Color buttonTextColor = Color.BLACK;
        
        addButton.setForeground(buttonTextColor);
        deleteButton.setForeground(buttonTextColor);
        completeButton.setForeground(buttonTextColor);
        darkModeButton.setForeground(buttonTextColor);
    
        // Update other components
        for (Component comp : frame.getContentPane().getComponents()) {
            updateComponentColors(comp, bgColor, fgColor);
        }
        
        SwingUtilities.updateComponentTreeUI(frame);
    }
    
    // Modify the button creation to keep dark text
    // Removed duplicate method to resolve the compile error
    
    private void updateComponentColors(Component comp, Color bg, Color fg) {
        if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            panel.setBackground(bg);
            panel.setForeground(fg);
            for (Component child : panel.getComponents()) {
                updateComponentColors(child, bg, fg);
            }
        }
        if (comp instanceof JLabel) {
            comp.setForeground(fg);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new TodoListApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
