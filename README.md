Mansib – Contributions and Implemented User Stories

I worked on implementing features related to notifications for organizers and administrative controls for the system. Most of my work involved connecting the app’s interface with Firebase so that the correct users or events could be retrieved and updated. These features help organizers communicate with participants and allow admins to manage users and events within the platform.

US 02.07.01 – Organizer sends notifications to all entrants on the waiting list

For this user story, I implemented the functionality that allows organizers to send a notification to all entrants who are currently on the waiting list for an event. The system retrieves the list of waiting list users from Firebase and then sends the notification message to each of them.

This feature allows organizers to quickly inform waiting list entrants about updates, such as if spots open up or if there are any changes related to the event.

US 02.07.02 – Organizer sends notifications to all selected entrants

For this feature, I implemented the ability for organizers to send notifications to entrants who have been selected for an event. The system retrieves the list of selected entrants from Firebase and broadcasts a notification to those users.

This helps ensure that selected participants are informed when they have been chosen and can respond or prepare for the event.

US 03.02.01 – Admin removes profiles

For this administrative feature, I worked on implementing the ability for an admin to remove user profiles from the system. The admin can select a user profile and remove it from the database.

This functionality helps maintain the platform by allowing administrators to remove inappropriate, inactive, or problematic accounts when needed.

US 03.04.01 – Admin browses events

For this user story, I implemented the feature that allows administrators to browse all events in the system. Event data is retrieved from Firebase and displayed in the admin interface in a list format.

This allows administrators to review events that have been created on the platform and monitor activity within the system.
