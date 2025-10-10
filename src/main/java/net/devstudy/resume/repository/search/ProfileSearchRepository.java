package net.devstudy.resume.repository.search;

import net.devstudy.resume.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProfileSearchRepository extends ElasticsearchRepository<Profile, String> {

    // Dynamische Multi-Match-Suche über viele Felder, AND-Operator, Fuzziness=1, Sort by uid DESC
    @Query(query = """
    {
      "multi_match": {
        "query": "?0",
        "fields": [
          "objective",
          "summary",
          "info",
          "certificates.name",
          "languages.name",
          "practics.company",
          "practics.position",
          "practics.responsibilities",
          "skills.value",
          "courses.name",
          "courses.school"
        ],
        "type": "best_fields",
        "operator": "and",
        "fuzziness": "1"
      }
    }
    """)
    Page<Profile> searchByQuery(String query, Pageable pageable);
}
