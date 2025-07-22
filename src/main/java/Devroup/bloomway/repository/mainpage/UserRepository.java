package Devroup.bloomway.repository.mainpage;

import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}