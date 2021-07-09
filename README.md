# Doodl.e

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
[Description of your app]

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:**
- **Mobile:**
- **Story:**
- **Market:**
- **Habit:**
- **Scope:**

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* [fill in your required user stories here]
* ...

**Optional Nice-to-have Stories**

* [fill in your required user stories here]
* ...

### 2. Screen Archetypes

* [list first screen here]
   * [list associated required story here]
   * ...
* [list second screen here]
   * [list associated required story here]
   * ...

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* [fill out your first tab]
* [fill out your second tab]
* [fill out your third tab]

**Flow Navigation** (Screen to Screen)

* [list first screen here]
   * [list screen navigation here]
   * ...
* [list second screen here]
   * [list screen navigation here]
   * ...

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
### Models
#### Doodle

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the doodle (default field) |
   | createdAt     | DateTime | date when doodle is created (default field) |
   | artist        | Pointer to User| doodle artist |
   | image         | File     | the doodle that the artist posted |
   | ParentDoodle  | Pointer to Doodle   | the parent to this doodle (null if this doodle has no parent) |
   | tailLength    | Number    | the length of the doodle's "tail" (i.e. how many doodles preceed it, including itself) (if not inGame, Doodle is unable to be randomly received after tailLength reaches 5)|
   | inGame        | Boolean   | whether or not the doodle is part of a game (if it is, it cannot be randomly received, and is deleted after the game ends) |
   | speedDoodle (optional) | File | data that can be used to recreate a speed-draw of the doodle's past |
   
#### Game

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | gameCode      | String   | unique code for the game |
   | players       | Array of Users | users in the game |
   | round         | Number   | current round number (starts at 1) |
   | doodles       | Array of Doodles | doodles submitted in the current round |
   
### Networking
#### List of network requests by screen
   - New Doodle Screen
      - (Create/POST) Create a new doodle object
   - Receive Random Doodle Screen
      - (Read/GET) Recieve a random existing doodle
      - (Create/POST) Create a new doodle object that is the child of the received doodle
   - Create Game Screen
      - (Create/POST) Create new game
        ```java
        Game game = new Game();
        String gameCode = generateRandomCode();
        game.setGameCode(gameCode);
        post.saveInBackground(new SaveCallback() { /* etc... */ });
      - (Read/GET) Get new game
        ```java
        ParseQuery<Game> query = ParseQuery.getQuery(Game.class);
        query.whereEqualTo("gameCode", gameCode);
        query.getFirstInBackground(new GetCallback<ParseObject>() { /* etc... */ });
   - Join Game Screen
      - (Update/PUT) Join game
      - (Read/GET) Get current game data (like current players)
   - Game Screen
      - (Update/PUT) Add new doodle to the game
   - Gallery Screen
      - (Read/GET) Get all doodles you've done
        ```java
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        query.include(Post.KEY_ARTIST);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
   - Profile Screen
      - (Update/PUT) Update username
      - (Update/PUT) Update password
      - (Update/PUT) Update twitter
#### [OPTIONAL:] Existing API Endpoints
##### Twitter API
- Base URL - [https://api.twitter.com/1.1](https://api.twitter.com/1.1)

   HTTP Verb | Endpoint | Description
   ----------|----------|------------
    `POST`   | /statuses/update.json | post new tweet
