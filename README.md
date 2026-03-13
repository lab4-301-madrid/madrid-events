# My Contributions and Implemented User Stories

In this project, I was responsible for implementing the organizer lottery and enrollment management features of the application. My work focused on the core lottery system that drives the entire purpose of the app, including drawing attendees from the waiting list, handling invitation responses, managing replacement draws, and giving organizers visibility into their final enrolled participant lists. These features form the backbone of the event lottery experience and were essential for the application to function as intended. In addition to my assigned stories, I took on responsibility for ensuring the final app was stable and functional for the Part 3 submission, which involved coordinating integration across features and resolving issues that emerged when combining everyone's work.

---

## US 02.05.01 – Organizer Sends Notification to Chosen Entrants

For this user story, I implemented the functionality that allows organizers to send notifications to entrants who have been selected through the lottery draw. When the system samples attendees from the waiting list, a notification is automatically dispatched to each chosen entrant informing them that they have won the lottery and are being invited to register for the event. These notifications are delivered through Firebase Cloud Messaging and the notification records are stored in Firebase so that delivery and history can be tracked. I made sure the notification content clearly communicated the event details and provided the entrant with the context they needed to take action on their invitation.

---

## US 02.05.02 – Organizer Sets the System to Sample a Specified Number of Attendees

For this feature, I built the lottery draw mechanism that organizers use to select a specified number of attendees from the waiting list. Organizers can input how many participants they want to invite and trigger the draw from the event management interface. The system then randomly samples that number of entrants from the pool of people on the waiting list and transitions them into an invited state. The draw logic was implemented to be fair and unbiased, pulling entrants at random while ensuring that the same entrant cannot be selected more than once for the same draw. The results of the draw are saved to Firebase so that the organizer and the rest of the system can act on them immediately.

---

## US 02.05.03 – Organizer Draws a Replacement Applicant After a Decline or Cancellation

For this user story, I implemented the replacement draw feature that allows organizers to bring in a new applicant when a previously selected entrant declines their invitation or cancels their registration. When a selected entrant rejects or is cancelled from an event, the system makes them eligible to be replaced and allows the organizer to trigger a redraw that samples one or more new entrants from the remaining waiting list. This ensures that event spots are not left empty due to declines and that entrants who were not initially selected still have a fair chance at being invited. The replacement draw follows the same random sampling logic as the initial draw and sends notifications to newly selected entrants automatically.

---

## US 02.06.03 – Organizer Views the Final List of Enrolled Entrants

For this feature, I implemented the screen and backend logic that gives organizers access to the final confirmed list of entrants who have accepted their invitation and officially enrolled in the event. This view is accessible from the organizer's event management panel and displays up to date enrollment information pulled from Firebase in real time. Organizers can use this list to know exactly who is attending their event, which is critical for planning and preparation. I made sure this view reflected the live state of enrollments so that any late acceptances or cancellations were captured accurately without requiring a manual refresh.

---

## Integration and Final App Stability for Part 3

Beyond my assigned stories, I took on the responsibility of ensuring the final application was stable and fully functional for the Part 3 project submission. This involved testing the end to end lottery flow across all roles, identifying and fixing integration issues that arose when combining features developed by different team members, and making sure that data written by one part of the system was correctly read and acted on by another. Several of the user stories depended on shared Firebase data structures, so I worked to align the data models and resolve inconsistencies. Ensuring the app worked as a complete and cohesive product was just as important as implementing individual features, and I treated that responsibility as a core part of my contribution to the team.
