

google.load("feeds", "1");

function feedLoaded() {

  var whichBlog = document.getElementById('blogtitle').innerHTML;
  whichBlog = whichBlog.slice(5,8);

  if (whichBlog === 'Bio') {
    var feed = new google.feeds.Feed("http://feeds.plos.org/plos/blogs/biologue");

  } else if (whichBlog === 'Spe') {
    var feed = new google.feeds.Feed("http://feeds.plos.org/plos/MedicineBlog");

  }
  //var feed = new google.feeds.Feed("http://feeds.plos.org/plos/blogs/biologue");
  feed.load(
    function (result) {
      var container = document.getElementById("blogrss");
      if (!result.error) {
        var html = "", docTitle, blogDiv, postQty, entry, postTitle, blogDate, postDescription,
          postPubDate, dateOptions, findDay, findMonth, tempDiv, blogImg;

        blogDiv = container.parentNode;
        docTitle = document.title.slice(5, 8);
        if (docTitle === 'Bio') {
          postQty = 4;
          blogDiv.style.height = "425px";
        } else {
          postQty = 2
        }
        for (var i = 0; i < postQty; i++) {

          entry = result.feed.entries[i];
          postTitle = entry.title;
          postDescription = entry.content;
          blogDate = new Date(entry.publishedDate);

          // IE10 & lower don't support options in toLocaleString, so use verbose method to display the date
          if (document.all) {
            var monthNames = new Array();
            monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
            findMonth = blogDate.getMonth();
            findDay = blogDate.getDay();
            postPubDate = monthNames[findMonth] + " " + findDay;

          } else {
            dateOptions = {month: "long", day: "numeric"};
            postPubDate = blogDate.toLocaleString("en-US", dateOptions);
          }

          // add ellipsis to titles that are cut off
          if (postTitle.length > 75) {
            postTitle = postTitle.slice(0, 70) + "&hellip;";
          }

          // create temporary div to traverse the description content to extract image src
          tempDiv = document.createElement('div');
          tempDiv.innerHTML = postDescription;
          blogImg = tempDiv.firstChild.src;
          if (blogImg == null) {
            blogImg = "resource/img/generic_blogfeed.png";
          }

          html += '<div><img class="postimg" src="' + blogImg + '" /><p class="postdate">Posted ' + postPubDate + '</p>' +
            '<p class="posttitle"><a href="' + entry.link + '">' + postTitle + '</a></p>' +
            '<p class="postauthor">' + entry.author + '</p></div>';

        }
        container.innerHTML = html;

      } else {
        container.innerHTML = "An error occurred while loading the blog posts.";
      }
    }
  );
}

