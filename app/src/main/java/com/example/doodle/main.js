Parse.Cloud.define("doodlenotification", (request) => {
    // Define the pushQuery
    var pushQuery = new Parse.Query(Parse.Installation);
    // Send to any installations with the user passed in
    pushQuery.equalTo('user', request.params.user);
    // Send the push notification
    return Parse.Push.send({
        where: pushQuery,
        data: {
            title: "Someone contributed to your doodle!",
            alert: "Come check it out!",
        }
    }, { useMasterKey: true });
});