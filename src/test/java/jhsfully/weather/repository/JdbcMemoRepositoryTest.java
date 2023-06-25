package jhsfully.weather.repository;

import jhsfully.weather.domain.Memo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JdbcMemoRepositoryTest {

    @Autowired
    JdbcMemoRepository jdbcMemoRepository;

    @Test
    void insertMemoTest(){
        //given
        Memo newMemo = new Memo(2, "This is new memos");
        //when
        jdbcMemoRepository.save(newMemo);
        //then
        Optional<Memo> result = jdbcMemoRepository.findById(2);
        assertEquals("This is new memos", result.get().getText());
    }

    @Test
    void findAllMemoTest(){
        //when
        List<Memo> memoList = jdbcMemoRepository.findAll();
        System.out.println(memoList);
        //then
        assertNotNull(memoList);
    }
}