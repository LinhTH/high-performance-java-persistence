package com.vladmihalcea.book.hpjp.linhth.association;

import com.vladmihalcea.book.hpjp.util.AbstractTest;
import org.junit.Test;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vlad Mihalcea
 */
public class BidirectionalOneToOneDuplicateJoinProblem extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Post.class,
                PostDetails.class,
        };
    }

    @Test
    public void testDuplicateJoinProblem() {
        doInJPA(entityManager -> {
            Post post = new Post("First post");
            PostDetails details = new PostDetails("John Doe");
            post.setDetails(details);
            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Post> query = builder.createQuery(Post.class);
            Root<Post> root = query.from(Post.class);

            query.select(root);
            root.join("details", JoinType.INNER);
            root.join("details", JoinType.INNER);
            // There are two inner join in statement, same as: https://discourse.hibernate.org/t/how-can-i-do-a-join-fetch-in-criteria-api/846
            //select bidirectio0_.id as id1_0_, bidirectio0_.title as title2_0_
            //from post bidirectio0_ inner join post_details bidirectio1_ on bidirectio0_.id=bidirectio1_.post_id
            //inner join post_details bidirectio2_ on bidirectio0_.id=bidirectio2_.post_id
            entityManager.createQuery(query).getResultList();
        });
    }


    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
        private PostDetails details;

        public Post() {}

        public Post(String title) {
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public PostDetails getDetails() {
            return details;
        }

        public void setDetails(PostDetails details) {
            if (details == null) {
                if (this.details != null) {
                    this.details.setPost(null);
                }
            }
            else {
                details.setPost(this);
            }
            this.details = details;
        }
    }

    @Entity(name = "PostDetails")
    @Table(name = "post_details")
    public static class PostDetails {

        @Id
        private Long id;

        @Column(name = "created_on")
        private Date createdOn;

        @Column(name = "created_by")
        private String createdBy;

        @OneToOne(fetch = FetchType.LAZY)
        @MapsId
        private Post post;

        public PostDetails() {}

        public PostDetails(String createdBy) {
            createdOn = new Date();
            this.createdBy = createdBy;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }
}
