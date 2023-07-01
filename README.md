# Ultimate Timer App


The Ultimate Timer App is a Java application that allows users to create, manage, and customize multiple timers. It offers a wide range of features, including the ability to pause, reset, modify, and remove timers. Additionally, the app supports functionalities such as running the timer backwards, creating multiple split timers (snapshotting the timer value and displaying it on the screen), and setting multiple alarms.

## Features

1. **Create Timers**: Users can create multiple timers with customizable names. Name customization of a timer is achieved by clicking on the name of the desired timer.

2. **Pause and Reset Timers**: Timers can be paused and reset to their initial values.

3. **Modify Timers**: The app allows users to modify timer values, providing flexibility in adjusting the timer value. The program checks if the given timer values are valid. When the program receives invalid input such as a minute value more than 60, the program automatically converts the given minute amount to hours.

4. **Reverse Timer**: Users have the option to run the timer backwards, enabling count-down functionality.

5. **Split Timers**: The app supports the creation of split timers, which capture and display the current timer value as a snapshot in a list format.

6. **Set Alarms**: Multiple alarms can be set, allowing users to define specific values. When the timer reaches the set alarm value, a notification sound will play and the timer value will be displayed in green. Alarm names can also be customized by clicking on the alarm names.

## Installation

To use the Ultimate Timer App, follow these steps:

1. Clone the repository to your local machine using the following command:

   ```bash
   git clone https://github.com/egegirit/ultimate-timer-app.git
   ```

2. Open the project in your preferred Java development environment.

3. Build the project and resolve any dependencies.

4. Run the `TimerApp` class to start the application.

## Usage

1. Upon launching the app, the main screen will be displayed with one already created timer.

2. To create a new timer, click on the "Add Timer" button. The name of the timer will be automatically generated, which can be customized later by the user.

3. Once a timer is created, it will be listed on the main screen. Use the available buttons next to each timer to perform various actions:

   - "Start/Pause Timer": Click this button to start or pause the timer.
   - "Reset Timer": This button will reset the timer to its initial value, which is 0. The reset button does not stop a running timer.
   - "Modify": Use this button to change the timer value.
   - "Split": Snapshots the current time and displays it in a list.
   - "Reset Splits": Removes all the split values in the panel.
   - "Set Alarm": Opens a window where the user can manage multiple alarms.
   - "Remove": Click this button to remove the timer.
   - "Reverse": Allows the timer to run backwards to be able to create countdown functionality.
   
4. To set an alarm, click on the "Set Alarm" button. Enter the desired alarm value and alarm name if needed. When the timer reaches an alarms value, a notification sound will play and the timer value will be displayed in green.

## Contribution

Contributions to the Ultimate Timer App are welcome! If you'd like to make any enhancements, bug fixes, or suggest new features, please follow these steps:

1. Fork the repository.

2. Create a new branch for your contribution.

3. Make the necessary changes and commit them.

4. Push your changes to your forked repository.

5. Submit a pull request, clearly describing the changes you've made.

Please ensure that your contributions align with the existing code style and follow best practices.

## License

The Ultimate Timer App is open-source software licensed under the [MIT License](LICENSE). Feel free to modify and distribute the app as per the license terms.

## Contact

If you have any questions, suggestions, or feedback, you can reach out to me at [egegirit@gmail.com](mailto:egegirit@gmail.com).

Happy timing with the Ultimate Timer App! 
