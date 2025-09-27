package code.uz.repository;

import code.uz.dto.FilterResultDTO;
import code.uz.dto.post.PostAdminFilterDTO;
import code.uz.dto.post.PostFilterDTO;
import code.uz.entity.PostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FilterPostRepository {
    private final EntityManager entityManager;

    public FilterResultDTO<PostEntity> filter(PostFilterDTO filterDTO, int page, int size) {
        StringBuilder visibleQuery = new StringBuilder(" where p.visible = true ");
        Map<String, Object> params = new HashMap<>();

        if (filterDTO.getQuery() != null) {
            visibleQuery.append(" and lower(p.title) like :query ");
            params.put("query", "%" + filterDTO.getQuery().toLowerCase() + "%");
        }

        Query selectQuery = entityManager.createQuery("select p from PostEntity p " + visibleQuery + " order by p.createdDate desc ");
        selectQuery.setFirstResult((page) * size);
        selectQuery.setMaxResults(size);
        params.forEach(selectQuery::setParameter);

        List<PostEntity> resultList = selectQuery.getResultList();

        String countQuery = "select count(p) from PostEntity p " + visibleQuery;
        Query count = entityManager.createQuery(countQuery);
        params.forEach(count::setParameter);
        Long totalElements = (Long) count.getSingleResult();

        return new FilterResultDTO<>(resultList, totalElements);
    }

    public FilterResultDTO<Object[]> filterAdminPosts(PostAdminFilterDTO filterDTO, int page, int size) {
        StringBuilder visibleQuery = new StringBuilder(" where p.visible = true ");
        Map<String, Object> params = new HashMap<>();

        if (filterDTO.getProfileQuery() != null) {
            visibleQuery.append(" and (lower(pr.name) like :profileQuery or lower(pr.username) like :profileQuery) "); // qavs quyilgani sababi visible=true and (... or ...) shu query bulishi uchun
            params.put("profileQuery", "%" + filterDTO.getProfileQuery().toLowerCase() + "%");
        }

        if (filterDTO.getPostQuery() != null) {
            visibleQuery.append(" and (lower(p.title) like :postQuery or p.id = :postId) "); // qavs quyilgani sababi visible=true and (... or ...) shu query bulishi uchun
            params.put("postQuery", "%" + filterDTO.getPostQuery().toLowerCase() + "%");
            params.put("postId", filterDTO.getPostQuery().toLowerCase());
        }

        Query selectQuery = entityManager.createQuery("select p.id as id, p.title as title, p.createdDate as createdDate, p.photoId as photoId, " +
                "pr.id as profileId, pr.username as username, pr.name as name from PostEntity p inner join p.profile as pr "
                + visibleQuery + " order by p.createdDate desc ");
        selectQuery.setFirstResult((page) * size);
        selectQuery.setMaxResults(size);
        params.forEach(selectQuery::setParameter);

        List<Object[]> resultList = selectQuery.getResultList();

        String countQuery = "select count(p) from PostEntity p inner join p.profile as pr " + visibleQuery;
        Query count = entityManager.createQuery(countQuery);
        params.forEach(count::setParameter);
        Long totalElements = (Long) count.getSingleResult();

        return new FilterResultDTO<>(resultList, totalElements);
    }
}
