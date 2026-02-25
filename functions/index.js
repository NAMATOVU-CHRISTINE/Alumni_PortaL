import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Send push notification when a new message is created
 */
export const sendMessageNotification = functions.firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const { chatId } = context.params;

    try {
      // Get recipient user data
      const recipientDoc = await db.collection("users").doc(message.receiverId).get();
      if (!recipientDoc.exists) {
        console.log("Recipient user not found");
        return;
      }

      const recipientData = recipientDoc.data();
      const recipientFcmToken = recipientData?.fcmToken;

      if (!recipientFcmToken) {
        console.log("Recipient FCM token not found");
        return;
      }

      // Get sender user data
      const senderDoc = await db.collection("users").doc(message.senderId).get();
      const senderData = senderDoc.data();
      const senderName = senderData?.fullName || message.senderName || "Alumni";

      // Check if recipient has message notifications enabled
      const notificationPrefs = recipientData?.notificationPreferences || {};
      if (notificationPrefs.messagesEnabled === false) {
        console.log("Message notifications disabled for recipient");
        return;
      }

      // Prepare message preview
      let messagePreview = message.messageText || "";
      if (message.messageType === "image") {
        messagePreview = "📷 Photo";
      } else if (message.messageType === "file") {
        messagePreview = `📎 ${message.fileName || "File"}`;
      } else if (messagePreview.length > 100) {
        messagePreview = messagePreview.substring(0, 100) + "...";
      }

      // Send notification
      await messaging.send({
        token: recipientFcmToken,
        notification: {
          title: senderName,
          body: messagePreview,
        },
        data: {
          chatId: chatId,
          senderId: message.senderId,
          receiverId: message.receiverId,
          type: "message",
          senderName: senderName,
          messageType: message.messageType || "text",
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "messages_channel",
            color: "#8BC34A", // Lime green
            tag: chatId, // Group notifications by chat
          },
        },
        apns: {
          headers: {
            "apns-priority": "10",
          },
          payload: {
            aps: {
              sound: "default",
              badge: 1,
              category: "MESSAGE",
              threadId: chatId, // Group notifications by chat on iOS
            },
          },
        },
      });

      console.log(`Message notification sent to ${message.receiverId}`);

      // Log to analytics
      await db.collection("notificationLogs").add({
        type: "message",
        recipientId: message.receiverId,
        senderId: message.senderId,
        chatId: chatId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "sent",
      });
    } catch (error) {
      console.error("Error sending notification:", error);
      
      // Log error
      await db.collection("notificationLogs").add({
        type: "message",
        recipientId: message.receiverId,
        senderId: message.senderId,
        chatId: chatId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "failed",
        error: error.message,
      });
    }
  });

/**
 * Send mentorship request notification
 */
export const sendMentorshipNotification = functions.firestore
  .document("mentorshipConnections/{connectionId}")
  .onCreate(async (snap, context) => {
    const connection = snap.data();

    try {
      // Get mentor user data
      const mentorDoc = await db.collection("users").doc(connection.mentorId).get();
      if (!mentorDoc.exists) {
        console.log("Mentor user not found");
        return;
      }

      const mentorData = mentorDoc.data();
      const mentorFcmToken = mentorData?.fcmToken;

      if (!mentorFcmToken) {
        console.log("Mentor FCM token not found");
        return;
      }

      // Get mentee data
      const menteeDoc = await db.collection("users").doc(connection.menteeId).get();
      const menteeData = menteeDoc.data();
      const menteeName = menteeData?.name || "Alumni";

      // Check if mentor has mentorship notifications enabled
      const notificationPrefs = mentorData?.notificationPreferences || {};
      if (notificationPrefs.mentorshipEnabled === false) {
        console.log("Mentorship notifications disabled for mentor");
        return;
      }

      // Send notification
      await messaging.send({
        token: mentorFcmToken,
        notification: {
          title: "New Mentorship Request",
          body: `${menteeName} has sent you a mentorship request`,
        },
        data: {
          connectionId: context.params.connectionId,
          type: "mentorship_request",
          menteeName: menteeName,
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "mentorship",
          },
        },
      });

      console.log(`Mentorship notification sent to ${connection.mentorId}`);
    } catch (error) {
      console.error("Error sending mentorship notification:", error);
    }
  });

/**
 * Send event update notification to all subscribers
 */
export const sendEventNotification = functions.firestore
  .document("events/{eventId}")
  .onUpdate(async (change, context) => {
    const newData = change.after.data();
    const oldData = change.before.data();

    // Only send if event details changed
    if (JSON.stringify(newData) === JSON.stringify(oldData)) {
      return;
    }

    try {
      // Get all users subscribed to events
      const usersSnapshot = await db
        .collection("users")
        .where("notificationPreferences.eventsEnabled", "==", true)
        .get();

      const tokens = [];
      usersSnapshot.forEach((doc) => {
        if (doc.data().fcmToken) {
          tokens.push(doc.data().fcmToken);
        }
      });

      if (tokens.length === 0) {
        console.log("No users subscribed to event notifications");
        return;
      }

      // Send multicast notification
      await messaging.sendMulticast({
        tokens: tokens,
        notification: {
          title: `Event Update: ${newData.title}`,
          body: newData.description?.substring(0, 100) || "Event has been updated",
        },
        data: {
          eventId: context.params.eventId,
          type: "event_update",
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "events",
          },
        },
      });

      console.log(`Event notification sent to ${tokens.length} users`);
    } catch (error) {
      console.error("Error sending event notification:", error);
    }
  });

/**
 * Send job posting notification to all subscribers
 */
export const sendJobNotification = functions.firestore
  .document("jobs/{jobId}")
  .onCreate(async (snap, context) => {
    const job = snap.data();

    try {
      // Get all users subscribed to job notifications
      const usersSnapshot = await db
        .collection("users")
        .where("notificationPreferences.jobsEnabled", "==", true)
        .get();

      const tokens = [];
      usersSnapshot.forEach((doc) => {
        if (doc.data().fcmToken) {
          tokens.push(doc.data().fcmToken);
        }
      });

      if (tokens.length === 0) {
        console.log("No users subscribed to job notifications");
        return;
      }

      // Send multicast notification
      await messaging.sendMulticast({
        tokens: tokens,
        notification: {
          title: `New Job: ${job.title}`,
          body: `${job.company} is hiring`,
        },
        data: {
          jobId: context.params.jobId,
          type: "job_posting",
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "jobs",
          },
        },
      });

      console.log(`Job notification sent to ${tokens.length} users`);
    } catch (error) {
      console.error("Error sending job notification:", error);
    }
  });


/**
 * Send notification when someone likes a post
 */
export const sendPostLikeNotification = functions.firestore
  .document("posts/{postId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const { postId } = context.params;

    // Check if likes increased
    const beforeLikes = before.likes?.length || 0;
    const afterLikes = after.likes?.length || 0;

    if (afterLikes <= beforeLikes) return;

    // Get the new liker
    const newLikers = after.likes.filter(id => !before.likes?.includes(id));
    if (newLikers.length === 0) return;

    const likerId = newLikers[0];
    const postAuthorId = after.authorId;

    // Don't notify if user liked their own post
    if (likerId === postAuthorId) return;

    try {
      // Get author's FCM token
      const authorDoc = await db.collection("users").doc(postAuthorId).get();
      const authorData = authorDoc.data();
      const fcmToken = authorData?.fcmToken;

      if (!fcmToken) return;

      // Check notification preferences
      const notificationPrefs = authorData?.notificationPreferences || {};
      if (notificationPrefs.likesEnabled === false) return;

      // Get liker's name
      const likerDoc = await db.collection("users").doc(likerId).get();
      const likerName = likerDoc.data()?.fullName || "Someone";

      // Send notification
      await messaging.send({
        token: fcmToken,
        notification: {
          title: "Post Liked",
          body: `${likerName} liked your post`,
        },
        data: {
          type: "post_like",
          postId: postId,
          likerId: likerId,
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "high_importance_channel",
            color: "#8BC34A",
          },
        },
      });

      console.log(`Post like notification sent to ${postAuthorId}`);
    } catch (error) {
      console.error("Error sending post like notification:", error);
    }
  });

/**
 * Send notification when someone views a story
 */
export const sendStoryViewNotification = functions.firestore
  .document("stories/{storyId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const { storyId } = context.params;

    // Check if viewedBy increased
    const beforeViews = before.viewedBy?.length || 0;
    const afterViews = after.viewedBy?.length || 0;

    if (afterViews <= beforeViews) return;

    // Get the new viewer
    const newViewers = after.viewedBy.filter(id => !before.viewedBy?.includes(id));
    if (newViewers.length === 0) return;

    const viewerId = newViewers[0];
    const storyAuthorId = after.authorId;

    // Don't notify if user viewed their own story
    if (viewerId === storyAuthorId) return;

    try {
      // Get author's FCM token
      const authorDoc = await db.collection("users").doc(storyAuthorId).get();
      const authorData = authorDoc.data();
      const fcmToken = authorData?.fcmToken;

      if (!fcmToken) return;

      // Check notification preferences
      const notificationPrefs = authorData?.notificationPreferences || {};
      if (notificationPrefs.storiesEnabled === false) return;

      // Get viewer's name
      const viewerDoc = await db.collection("users").doc(viewerId).get();
      const viewerName = viewerDoc.data()?.fullName || "Someone";

      // Send notification
      await messaging.send({
        token: fcmToken,
        notification: {
          title: "Story Viewed",
          body: `${viewerName} viewed your story`,
        },
        data: {
          type: "story_view",
          storyId: storyId,
          viewerId: viewerId,
        },
        android: {
          priority: "default",
          notification: {
            sound: "default",
            channelId: "high_importance_channel",
            color: "#8BC34A",
          },
        },
      });

      console.log(`Story view notification sent to ${storyAuthorId}`);
    } catch (error) {
      console.error("Error sending story view notification:", error);
    }
  });

/**
 * Send notification when someone follows a user
 */
export const sendFollowerNotification = functions.firestore
  .document("followers/{followId}")
  .onCreate(async (snap, context) => {
    const followData = snap.data();
    const followedUserId = followData.followedUserId;
    const followerId = followData.followerId;

    try {
      // Get followed user's FCM token
      const userDoc = await db.collection("users").doc(followedUserId).get();
      const userData = userDoc.data();
      const fcmToken = userData?.fcmToken;

      if (!fcmToken) return;

      // Check notification preferences
      const notificationPrefs = userData?.notificationPreferences || {};
      if (notificationPrefs.followersEnabled === false) return;

      // Get follower's name
      const followerDoc = await db.collection("users").doc(followerId).get();
      const followerName = followerDoc.data()?.fullName || "Someone";

      // Send notification
      await messaging.send({
        token: fcmToken,
        notification: {
          title: "New Follower",
          body: `${followerName} started following you`,
        },
        data: {
          type: "new_follower",
          followerId: followerId,
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "high_importance_channel",
            color: "#8BC34A",
          },
        },
      });

      console.log(`Follower notification sent to ${followedUserId}`);
    } catch (error) {
      console.error("Error sending follower notification:", error);
    }
  });
