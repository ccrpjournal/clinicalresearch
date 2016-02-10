package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CommentService {

  /**
   * Retrieve a comment and wire in additional data needed for display.
   *
   * @param commentId the ID of a comment
   * @return a representation of the comment's content and metadata
   * @throws IOException
   * @throws CommentNotFoundException if the comment does not exist
   */
  Map<String, Object> getComment(String commentId) throws IOException;

  /**
   * Retrieve all comments belonging to a single parent article and wire in additional data needed for display.
   *
   * @param articleDoi the article DOI
   * @return a list of representations of the comments' content and metadata
   * @throws IOException
   * @throws EntityNotFoundException if the article does not exist
   */
  List<Map<String, Object>> getArticleComments(String articleDoi) throws IOException;

  public static class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String commentId, EntityNotFoundException cause) {
      super("No comment with ID: " + commentId, cause);
    }
  }

}
