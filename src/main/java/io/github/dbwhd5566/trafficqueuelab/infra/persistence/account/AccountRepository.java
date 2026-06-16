package io.github.dbwhd5566.trafficqueuelab.infra.persistence.account;

import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
