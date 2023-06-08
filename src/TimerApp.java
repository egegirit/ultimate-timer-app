import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Dimension;
import javax.swing.JScrollPane;

public class TimerApp extends JFrame implements AutoCloseable {
    private final JPanel mainPanel;
    private final List<TimerPanel> timerPanels;
    private final JLabel dateTimeLabel;
    private Timer dateTimeTimer;

    // Font size of the timer
    private static final int timerNameFontSize = 16;
    // Initial window size of the program
    private static int initialWidth = 1100;
    private static int initialHeight = 400;

    public TimerApp() {
        // Set title of window and closing behavior
        setTitle("The Ultimate Timer App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Add the add timer button to the main GUI at the bottom of the screen with full width
        JButton addTimerButton = new JButton("Add Timer");
        addTimerButton.addActionListener(e -> addTimer());
        add(addTimerButton, BorderLayout.SOUTH);

        timerPanels = new ArrayList<>();
        // Open the window with an already created timer
        addTimer();

        // Display current date and time on top of GUI
        dateTimeLabel = new JLabel("Current date and time: ");
        dateTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(dateTimeLabel, BorderLayout.NORTH);
        startDateTimeTimer();

        // Set initial window size of main GUI
        setInitialSize(initialWidth, initialHeight);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Method to set the initial size of the main application window
    public void setInitialSize(int width, int height) {
        initialWidth = width;
        initialHeight = height;
        setSize(initialWidth, initialHeight);
    }

    // Method to add a new timer panel to the main panel
    private void addTimer() {
        String timerName = "Timer " + (timerPanels.size() + 1) + ":";
        TimerPanel timerPanel = new TimerPanel(timerName);
        timerPanels.add(timerPanel);
        mainPanel.add(timerPanel);
        mainPanel.revalidate();
    }

    // Method to remove a timer panel from the main panel
    private void removeTimer(TimerPanel timerPanel) {
        int choice = JOptionPane.showConfirmDialog(
                TimerApp.this,
                "Are you sure you want to remove this timer?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            timerPanel.stopTimer();
            timerPanels.remove(timerPanel);
            mainPanel.remove(timerPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            // Adjust the frame size to fit the updated content
            // pack();
        }
    }

    // Class representing an individual timer panel
    private class TimerPanel extends JPanel {

        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;
        private final JLabel timeLabel;
        private final JLabel nameLabel;
        private final JButton startPauseButton;
        private final JCheckBox reverseCheckbox;
        private final DefaultListModel<String> splitListModel;
        private final JList<String> splitList;
        private static final Dimension LIST_AREA_SIZE = new Dimension(190, 90);
        private int splitCounter;
        private boolean alarmPanelIsOpen;
        private final ArrayList<Alarm> alarms;


        private ScheduledExecutorService scheduledExecutor;

        public TimerPanel(String name) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setPreferredSize(new Dimension(400, 100));

            nameLabel = new JLabel(name);
            nameLabel.setForeground(Color.BLUE);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, timerNameFontSize));
            // Make the name of the timer clickable and editable
            nameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String currentTimerName = nameLabel.getText();
                    String currentNameWithoutColon = currentTimerName.endsWith(":")
                            ? currentTimerName.substring(0, currentTimerName.length() - 1)
                            : currentTimerName;

                    String newTimerName = JOptionPane.showInputDialog(
                            TimerApp.this,
                            "Enter a new name for the timer:",
                            currentNameWithoutColon
                    );

                    if (newTimerName != null && !newTimerName.isEmpty()) {
                        String updatedTimerName = newTimerName.endsWith(":")
                                ? newTimerName
                                : newTimerName + ":";
                        nameLabel.setText(updatedTimerName);
                    }
                }
            });
            add(nameLabel);

            alarms = new ArrayList<>();

            // Display the value of timer and assign a fixed size to the timer to prevent it from pushing other GUI elements
            timeLabel = new JLabel(formatTime(0, 0, 0, 0));
            timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 24));
            // Use a fixed-size JLabel for the timer area
            timeLabel.setPreferredSize(new Dimension(160, 50));
            add(timeLabel);

            startPauseButton = new JButton("Start Timer");
            // Since we combine the start and pause buttons and change the text of the button,
            // set the size of the button to a fixed value to prevent the button to grow when the text changes
            startPauseButton.setPreferredSize(new Dimension(110, 26));
            startPauseButton.addActionListener(e -> {
                if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
                    startTimer();
                } else {
                    pauseTimer();
                }
            });
            add(startPauseButton);

            JButton resetButton = new JButton("Reset Timer");
            resetButton.addActionListener(e -> resetTimer());
            add(resetButton);

            JButton modifyButton = new JButton("Modify");
            modifyButton.addActionListener(e -> modifyTimer());
            add(modifyButton);

            JButton splitButton = new JButton("Split");
            splitButton.addActionListener(e -> createSplit());
            add(splitButton);

            JButton resetSplitsButton = new JButton("Reset Splits");
            resetSplitsButton.addActionListener(e -> resetSplits());
            add(resetSplitsButton);

            JButton setAlarmButton = new JButton("Set Alarm");
            setAlarmButton.addActionListener(e -> openAlarmManager(this));
            add(setAlarmButton);

            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeTimer(this));
            add(removeButton);

            reverseCheckbox = new JCheckBox("Reverse", false);
            add(reverseCheckbox);

            splitListModel = new DefaultListModel<>();
            splitList = new JList<>(splitListModel);

            // splitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            splitList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            // Set the preferred size for the JList area
            JScrollPane scrollPane = new JScrollPane(splitList);
            scrollPane.setPreferredSize(LIST_AREA_SIZE);
            add(scrollPane);
            splitList.setComponentPopupMenu(createPopupMenu());

        }

        // Method to open the alarm manager window
        private void openAlarmManager(TimerPanel timerPanel) {
            if(!alarmPanelIsOpen){
                new AlarmManager(timerPanel);
                //alarmManager.setVisible(true);
            }
        }

        // Helper method to update the split counter labels
        private void updateSplitCounters() {
            int splitCount = splitListModel.size();
            for (int i = 0; i < splitCount; i++) {
                String splitLabel = splitListModel.getElementAt(i);
                String[] splitLabelParts = splitLabel.split(":");
                int counter = i + 1; // Adjusted split counter value
                splitLabelParts[0] = "Split " + counter;
                splitListModel.set(i, String.join(":", splitLabelParts));
            }
        }

        // Right-clicking a split will open a remove popup
        private JPopupMenu createPopupMenu() {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem removeItem = new JMenuItem("Remove Splits");
            removeItem.addActionListener(e -> removeSelectedSplits());
            popupMenu.add(removeItem);
            return popupMenu;
        }

        // Remove the selected splits and adjust the counter value of remaining splits
        private void removeSelectedSplits() {
            int[] selectedIndices = splitList.getSelectedIndices();
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                splitListModel.remove(selectedIndices[i]);
            }
            // After removing a split, update the counter values to display them sequentially
            updateSplitCounters();
        }

        // Snapshot the current timer value, assign a counter value to it and display it on the JList
        private void createSplit() {
            // Get the timer values
            int splitHours = hours;
            int splitMinutes = minutes;
            int splitSeconds = seconds;
            int splitMilliseconds = milliseconds;

            // Increment the split counter
            splitCounter++;

            // Create the split label
            String splitLabel = "Split " + splitCounter + ": " + formatTime(splitHours, splitMinutes, splitSeconds, splitMilliseconds);
            splitListModel.addElement(splitLabel);

            // Sort the splits by their counter values
            List<String> sortedSplits = new ArrayList<>();
            for (int i = 0; i < splitListModel.size(); i++) {
                sortedSplits.add(splitListModel.getElementAt(i));
            }
            sortedSplits.sort(Comparator.comparingInt(this::getSplitCounterFromLabel));

            // Update the split list with the sorted splits
            splitListModel.clear();
            for (String split : sortedSplits) {
                splitListModel.addElement(split);
            }
        }


        // Helper method to extract the split counter value from the split label
        private int getSplitCounterFromLabel(String splitLabel) {
            String[] parts = splitLabel.split(":");
            if (parts.length > 0) {
                String counter = parts[0].trim();
                try {
                    return Integer.parseInt(counter);
                } catch (NumberFormatException e) {
                    // Handle invalid split labels if necessary
                }
            }
            return 0; // Default value if split counter cannot be extracted
        }


        private void resetSplits() {
            splitListModel.clear();
            splitCounter = 0;
        }



        // Method to start or resume the timer
        private void startTimer() {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleAtFixedRate(() -> {
                // milliseconds += reverseCheckbox.isSelected() ? -10 : 10;

                if (reverseCheckbox.isSelected()){
                    milliseconds += -10;
                    System.out.println(milliseconds);
                    if (milliseconds <= -1000) {
                        seconds -= -(milliseconds / 1000);
                        milliseconds %= 1000;
                    }

                    if (seconds <= -60) {
                        minutes -= -(seconds / 60);
                        seconds %= 60;
                    }

                    if (minutes <= -60) {
                        hours -= -(minutes / 60);
                        minutes %= 60;
                    }

                }
                else
                {
                    milliseconds += 10;

                    if (milliseconds >= 1000 || milliseconds <= -1000) {
                        seconds += milliseconds / 1000;
                        milliseconds %= 1000;
                    }

                    if (seconds >= 60 || seconds <= -60) {
                        minutes += seconds / 60;
                        seconds %= 60;
                    }

                    if (minutes >= 60 || minutes <= -60) {
                        hours += minutes / 60;
                        minutes %= 60;
                    }
                }

                // Check if an alarm timer is reached
                checkAlarmNotification();

                SwingUtilities.invokeLater(() -> timeLabel.setText(formatTime(hours, minutes, seconds, milliseconds)));
            }, 0, 10, TimeUnit.MILLISECONDS);

            // The start timer button becomes pause timer after clicking the start button
            startPauseButton.setText("Pause Timer");
        }

        // If the timer value reaches one of the alarm values, play notification sound and color the timer value to green
        public void checkAlarmNotification(){
            for (Alarm alarm : this.alarms) {
                if (alarm.getHours() == hours && alarm.getMinutes() == minutes && alarm.getSeconds() == seconds && alarm.getMilliseconds() == milliseconds) {
                    AudioPlayer audioPlayer = new AudioPlayer();
                    audioPlayer.playNotificationSound(-10.0f);
                    alarm.getAlarmTimeLabel().setForeground(Color.GREEN);
                }
            }
        }

        // Method to stop the timer if it is running
        public void stopTimer() {
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdown();
            }
        }

        // Method to pause the timer
        private void pauseTimer() {
            if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdown();
                scheduledExecutor = null;
                startPauseButton.setText("Start Timer");
            }
        }


        // Method to reset the timer
        private void resetTimer() {
            // pauseTimer();
            hours = minutes = seconds = milliseconds = 0;
            timeLabel.setText(formatTime(0, 0, 0, 0));
        }

        // Method to modify the timer values
        private void modifyTimer() {
            String input = JOptionPane.showInputDialog(
                    TimerApp.this,
                    "Enter the new timer values (HH:MM:SS:SS):",
                    formatTime(hours, minutes, seconds, milliseconds)
            );

            if (input != null) {
                String[] parts = input.split(":");

                if (parts.length == 4) {
                    try {
                        int newHours = Integer.parseInt(parts[0]);
                        int newMinutes = Integer.parseInt(parts[1]);
                        int newSeconds = Integer.parseInt(parts[2]);
                        int newMilliseconds = Integer.parseInt(parts[3]);

                        hours = newHours;
                        minutes = newMinutes;
                        seconds = newSeconds;
                        // Milliseconds must be multiplied with 10 because the actual milliseconds value
                        // is displayed with 2 digits, but we display 2 digits and ask the user for 2 digits
                        milliseconds = newMilliseconds * 10;

                        timeLabel.setText(formatTime(hours, minutes, seconds, milliseconds));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(TimerApp.this,
                                "Invalid input format. Please use HH:MM:SS:SS format.",
                                "Invalid Input",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(TimerApp.this,
                            "Invalid input format. Please use HH:MM:SS:SS format.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Window that manages the alarms
    class AlarmManager extends JFrame {

        JPanel alarmsPanel;
        JButton addAlarmButton;
        TimerPanel timerPanel;

        public AlarmManager(TimerPanel timerPanel) {
            // The alarm belongs to the selected timer panel object
            this.timerPanel = timerPanel;
            // Avoid opening multiple alarm windows by clicking the Set Alarm button multiple times
            timerPanel.alarmPanelIsOpen = true;

            // Implement the alarm manager UI and functionality here
            JFrame manageAlarmsFrame = new JFrame("Manage Alarms");
            manageAlarmsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            manageAlarmsFrame.setLayout(new BorderLayout());

            // Window listener to set the alarmPanelIsOpen flag when the window is closed
            WindowListener windowListener = new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    timerPanel.alarmPanelIsOpen = false;
                }
            };
            manageAlarmsFrame.addWindowListener(windowListener);

            alarmsPanel = new JPanel();
            alarmsPanel.setLayout(new BoxLayout(alarmsPanel, BoxLayout.Y_AXIS));

            // Retrieve the stored alarms for the selected timer
            ArrayList<Alarm> alarms = timerPanel.alarms;
            // Add one alarm by default when the window opens for the first time or when no alarm exists
            if (alarms.size() == 0){
                addNewAlarm(timerPanel);
            } else {
                // Iterate over the alarms and display them in the UI
                for (Alarm alarm : alarms) {
                    // Display the alarm information in the UI
                    addExistingAlarmToPanel(alarm, timerPanel);
                }
            }

            addAlarmButton = new JButton("Add Alarm");
            addAlarmButton.addActionListener(e -> addNewAlarm(timerPanel));

            // Set the add alarm button to cover the entire width
            addAlarmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            addAlarmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addAlarmButton.getPreferredSize().height));
            // Create the vertical box
            Box box = Box.createVerticalBox();
            // Create rigid area for vertical spacing
            Component rigidArea = Box.createVerticalStrut(10);
            // Add rigid area and button to the box
            box.add(rigidArea);
            // Add glue to the box to make the button appear at the bottom
            box.add(Box.createVerticalGlue());
            // Add the button to the box
            box.add(addAlarmButton);
            // Add the button to the alarm manager window
            alarmsPanel.add(addAlarmButton, BorderLayout.SOUTH);

            manageAlarmsFrame.add(alarmsPanel, BorderLayout.NORTH);
            manageAlarmsFrame.add(box, BorderLayout.SOUTH);
            manageAlarmsFrame.pack();
            manageAlarmsFrame.setVisible(true);

            setLocationRelativeTo(TimerApp.this);
        }

        private void addExistingAlarmToPanel(Alarm alarm, TimerPanel timerPanel){
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> editAlarmTimerValue(alarm));

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> removeAlarm(alarm.getNameLabelOfAlarm(), timerPanel));
            JPanel alarmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            // Order of the Alarm buttons
            alarmPanel.add(alarm.getNameLabelOfAlarm());
            alarmPanel.add(alarm.getAlarmTimeLabel());
            alarmPanel.add(editButton);
            alarmPanel.add(deleteButton);
            alarmsPanel.add(alarmPanel);
            alarmsPanel.revalidate();
        }

        private void addNewAlarm(TimerPanel timerPanel) {
            int alarmCount = alarmsPanel.getComponentCount() -1; // Subtract 1 for the "Add Alarm" button
            // First init of alarmCount is -1, which is wrong
            if ((alarmCount + 1) == 0){
                alarmCount = 0;
            }

            Alarm alarm = new Alarm();
            // Set name of the alarm
            String nameOfAlarm = "Alarm " + (alarmCount + 1);
            alarm.setNameLabelOfAlarm(new JLabel(nameOfAlarm + ":"));
            alarm.getNameLabelOfAlarm().setForeground(Color.BLUE);
            alarm.getNameLabelOfAlarm().setFont(alarm.getNameLabelOfAlarm().getFont().deriveFont(Font.BOLD, timerNameFontSize));
            // When user clicks on alarm name, edit window opens
            alarm.getNameLabelOfAlarm().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String currentTimerName = alarm.getNameLabelOfAlarm().getText();
                    String currentNameWithoutColon = currentTimerName.endsWith(":")
                            ? currentTimerName.substring(0, currentTimerName.length() - 1)
                            : currentTimerName;

                    String newTimerName = JOptionPane.showInputDialog(
                            TimerApp.this,
                            "Enter a new name for the alarm:",
                            currentNameWithoutColon
                    );

                    if (newTimerName != null && !newTimerName.isEmpty()) {
                        String updatedName = newTimerName.endsWith(":")
                                ? newTimerName
                                : newTimerName + ":";
                        alarm.getNameLabelOfAlarm().setText(updatedName);
                    }
                }
            });

            alarm.setAlarmTimeLabel(new JLabel(formatTime(0, 0, 0, 0)));
            // alarm.alarmTimeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 24));
            // Use a fixed-size JLabel for the timer area
            alarm.getAlarmTimeLabel().setPreferredSize(new Dimension(80, 50));

            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> editAlarmTimerValue(alarm));

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> removeAlarm(alarm.getNameLabelOfAlarm(), timerPanel));
            JPanel alarmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // Store the created alarm in the timer panel so that if the user closes the "Set Alarm" window,
            // the alarms won't be deleted
            timerPanel.alarms.add(alarm);

            // Order of the Alarm buttons
            alarmPanel.add(alarm.getNameLabelOfAlarm());
            alarmPanel.add(alarm.getAlarmTimeLabel());
            alarmPanel.add(editButton);
            alarmPanel.add(deleteButton);
            alarmsPanel.add(alarmPanel, alarmCount);
            alarmsPanel.revalidate();
            alarmsPanel.repaint();
        }

        private void editAlarmTimerValue(Alarm alarm){
            String input = JOptionPane.showInputDialog(
                    TimerApp.this,
                    "Enter the new timer values (HH:MM:SS:SS):",
                    formatTime(alarm.getHours(), alarm.getMinutes(), alarm.getSeconds(), alarm.getMilliseconds())
            );

            if (input != null) {
                String[] parts = input.split(":");

                if (parts.length == 4) {
                    try {
                        int newHours = Integer.parseInt(parts[0]);
                        int newMinutes = Integer.parseInt(parts[1]);
                        int newSeconds = Integer.parseInt(parts[2]);
                        int newMilliseconds = Integer.parseInt(parts[3]);

                        // Range validation for editing alarm timer
                        if ((newMilliseconds < 0 || newMilliseconds > 1000) || (newSeconds < 0 || newSeconds > 60) || (newMinutes < 0 || newMinutes > 60) || (newHours < 0 || newHours > 23) ){
                            JOptionPane.showMessageDialog(TimerApp.this,
                                    "Invalid input format. Please use HH:MM:SS:SS format.",
                                    "Invalid Input",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            alarm.setHours(newHours);
                            alarm.setMinutes(newMinutes);
                            alarm.setSeconds(newSeconds);
                            // Milliseconds must be multiplied with 10 because the actual milliseconds value
                            // is displayed with 2 digits, but we display 2 digits and ask the user for 2 digits
                            alarm.setMilliseconds(newMilliseconds * 10);
                            alarm.getAlarmTimeLabel().setText(formatTime(alarm.getHours(), alarm.getMinutes(), alarm.getSeconds(), alarm.getMilliseconds()));
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(TimerApp.this,
                                "Invalid input format. Please use HH:MM:SS:SS format.",
                                "Invalid Input",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(TimerApp.this,
                            "Invalid input format. Please use HH:MM:SS:SS format.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // Remove alarm from the selected timer
        // Todo: need to ensure each alarm has unique name?
        private void removeAlarm(JLabel alarmLabel, TimerPanel timerPanel) {
            Component[] components = alarmsPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel alarmPanel) {
                    if (alarmPanel.getComponent(0) == alarmLabel) {
                        alarmsPanel.remove(alarmPanel);
                        alarmsPanel.revalidate();
                        alarmsPanel.repaint();
                        break;
                    }
                }
            }
            for (Alarm alarm : timerPanel.alarms) {
                if (alarm.getNameLabelOfAlarm() == alarmLabel) {
                    timerPanel.alarms.remove(alarm);
                    break;
                }
            }

        }
    }

    // Method to start a timer for updating the current date and time label
    private void startDateTimeTimer() {
        dateTimeTimer = new Timer(1000, e -> {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
            String formattedDateTime = currentDateTime.format(formatter);
            dateTimeLabel.setText("Current date and time: " + formattedDateTime);
        });

        dateTimeTimer.start();
    }

    // Helper method to format the time values
    private String formatTime(int hours, int minutes, int seconds, int milliseconds) {
        DecimalFormat format = new DecimalFormat("00");
        return format.format(hours) + ":" + format.format(minutes) + ":" +
                format.format(seconds) + ":" + format.format(milliseconds / 10);
    }

    // Closeable implementation to stop the date and time timer when closing the application
    @Override
    public void close() {
        if (dateTimeTimer != null) {
            dateTimeTimer.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerApp::new);
    }
}

