Summary of the app:
- Displays response from the Flickr API in a 3 column grid
- Tapping on an image expands it to the whole width, showing the title, owner, and any tags associated with the image

- Response is either /getRecent by default, or uses two potential queries:
  
- #1 A search box at the top of the screen, that will contact /search with the text input or
- #2 Tapping a tag displayed below an image will call /search with the tag. These queries are not exclusive

- The list automatically loads the next 100 images (1 page worth) when the bottom is reached. These images are added to the list to allow the user to scroll back up to see previous images.

Pending Issues:
- The compose does not recompose when an image is expanded to show its true height. The height is shown upon recomposing the box without closing and opening it again
- I suspect this is due to a lack of state, or by using .then() for the height modifier
- The app needs a theme/styling, right now it is using basic colors loaded by default
- Compose previews and unit tests need to be added to the app

- AI Use:
- No external AI assistant was used in the project, IntelliJ hints were utilized to save on time. A video of that can be found in: Videos/IntelliJ_AI_Assistant.mp4
