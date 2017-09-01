package banking;

import banking.model.Account;
import banking.model.Money;
import banking.persistence.FileRepository;
import banking.persistence.Repository;
import com.google.gson.Gson;
import spark.Service;

import java.io.File;
import java.util.UUID;

public class RestApi {

  private static final int PORT = 8080;

  private Repository<Account> accountRepository;

  public static void main(String[] strings) {
    new RestApi(
        new FileRepository<>(Account.class, new File("accounts.json")))
            .run(Service.ignite());
  }

  public RestApi(Repository<Account> accountRepository) {
    this.accountRepository = accountRepository;
  }

  public void run(Service spark) {
    spark.port(PORT);

    spark.post("/accounts", (request, response) -> {
      Money startingBalance = new Gson().fromJson(request.body(), Money.class);
      Account account = new Account(startingBalance);
      accountRepository.save(account);
      return account;
    }, new Gson()::toJson);

    spark.get("/accounts/:id/balance", (request, response) -> {

      UUID id = UUID.fromString(request.params("id"));

      return accountRepository
          .findOne(id)
          .map(account -> account.getBalance())
          .orElse(null);

    }, new Gson()::toJson);


    spark.after((request, response) -> {
      response.type("application/json");
    });
  }

}
