MongoTemplate은 `find`, `findAndModify`, `findAndReplace`, `findOne`, `insert`, `remove`, `save`, `update`, `updateMulti` 등 편리한 기능들을 제공함.
MongoDB driver 대신에 MongoOperations을 사용하는 구조인데, `Document` 타입 대신에 Java Object를 사용함으로써 더욱 다루기 쉬워지고, 다양한 API를 사용할 수 있음. (Query, Criteria 등등)

MongoTemplate이 사용하는 default Converter는 `MappingMongoConverter`임.

`WriteResultChecking` 값은 기본적으로 NONE이지만 EXCEPTION으로 변경 가능. 변경하면, 몽고 디비 업데이트 실패 시에 예외를 발생시킴.

```java
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    private String username;

    private String email;

    public Account(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Account{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}

```


```java
@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    MongoTemplate template;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Drop Collection
        template.dropCollection(Account.class);

        // Insert
        Account account1 = new Account("youngjae park", "youngjae.p");
        Account account2 = new Account("youngjae p", "youngjae.p");
        Account account3 = new Account("youngjae", "youngjae.park");

        template.insert(account1);
        template.insert(account2);
        template.insert(account3);

        // Find
        Account findAccount = template.findById(account1.getId(), Account.class);

        // Update
        template.updateFirst(Query.query(Criteria.where("username").is("youngjae park")),
                             Update.update("email", "NONE"), Account.class);

        // Delete
        template.remove(account2);

        // find All
        template.findAll(Account.class).forEach(System.out::println);

    }
}
```

```
[실행결과]
Account{id='5f435e0c78b669750f5ef91a', username='youngjae park', email='NONE'}
Account{id='5f435e0c78b669750f5ef91c', username='youngjae', email='youngjae.park'}
```



Document의 ID는 객체의 @id 애노테이션이 붙은 곳과 매핑됩니다. String, BigInteger, ObjectId 세 가지 사용 가능.


다큐먼트가 아닐 경우 저장할 때 Cascading 가능함. 다큐먼트일 경우 하나씩 insert(or save)를 호출해야 함.
save는 아이디가 이미 존재할 경우, Update하며, 아이디가 없는 경우 Insert함. (Upsert)

- insert(Object obj) : obj를 컬렉션에 저장함.
- insert(Object obj, String collectionName) : obj를 collectionName에 저장함.


```java
    @Test
    void saveCascading() {
        Child child = new Child("child");
        Parent parent = new Parent("parent", child);

        mongoTemplate.save(parent);
    }
```



- updateFirst : 쿼리 문서와 일치하는 첫 번째 문서를 업데이트
- updateMulti : 쿼리 문서와 일치하는 모든 문서 업데이트


- findAndModify : 데이터를 찾은 후에 수정이 가능하다. 해당하는 모든 데이터를 수정함. 리턴 값으로 객체를 반환하지만, 해당 객체는 oldValue임에 주의하자. 필요하다면 다시 조회해와야함.
    - FindAndModifyOptions의 returnNew()를 true로 설정하면 바로 새 데이터 반환하게 할 수 있음.

```java
    @Test
    void findAndModify() {

        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);

        // return OldValue
        Parent oldParent = mongoTemplate.findAndModify(Query.query(Criteria.where("name").is("parent")),
                                                       new Update().inc("age", 1), Parent.class);

        assertEquals(32, oldParent.getAge());
        assertEquals(3, oldParent.getChild().getAge());

        // return newValue
        Parent findParent =  mongoTemplate.findOne(Query.query(Criteria.where("name").is("parent")), Parent.class);

        assertEquals(33, findParent.getAge());
        assertEquals(3, findParent.getChild().getAge());
    }
```

- 이렇게도 할 수 있음.

```java
    @Test
    void findAndModify() {

        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);

        // return OldValue
        Parent oldParent = mongoTemplate.update(Parent.class)
                                .matching(Criteria.where("name", "parent"))
                                .apply(new Update().inc("age",1))
                                .findAndModifyValue();

        // return newValue
        Parent findParent =  mongoTemplate.update(Parent.class)
                                .matching(Criteria.where("name", "parent"))
                                .apply(new Update().inc("age",1))
                                .withOptions(FindAndModifyOptions.options().returnNew(true))
                                .findAndModifyValue();
    }


```


- Query 사용해보기.

```java
    @Test
    void queryDocumentsInACollection() {
        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        mongoTemplate.save(parent);
        mongoTemplate.save(parent2);
        mongoTemplate.save(parent3);    //same content with parent2

        List<String> result = mongoTemplate.query(Parent.class)
                                           .distinct("name")
                                           .matching(Query.query(Criteria.where("age").gt(30)))
                                           .as(String.class)
                                           .all();

        assertEquals(2, result.size());
        assertEquals("parent", result.get(0));
        assertEquals("parent2", result.get(1));
    }
```

- 데이터가 중복되었을 때, distinct로 걸러낼 수 있지만, 반환타입에도 제약이 생김.


## Working with Spring Data Repositories

```java
public interface ParentRepository extends CrudRepository<Parent, String> {

    List<Parent> findByName(String name);
}
```

```java
    @Test
    void findByName() {
        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        // save()는 CrudRepository에 이미 존재하기 때문에 Repository Proxy를 바로 사용할 수 있음..
        parentRepository.save(parent);
        parentRepository.save(parent2);
        parentRepository.save(parent3);    //same content with parent2

        // 메서드 이름 규칙에 따라 Proxy에 기능을 구현해줌.
        List<Parent> parents = parentRepository.findByName("parent2");

        assertEquals(2, parents.size());
    }
```

- Child의 이름으로도 찾을 수 있음. findChildName --> x.child.name을 의미함.

```java

    @Test
    void findByChildName() {
        initTestData();

        List<Parent> parents = parentRepository.findByChildName("child2");

        assertEquals(2, parents.size());

    }

    public void initTestData() {
        mongoTemplate.dropCollection(Parent.class);
        mongoTemplate.dropCollection(Child.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        childRepository.saveAll(Arrays.asList(child, child2, child3));
        parentRepository.saveAll(Arrays.asList(parent, parent2, parent3));
    }

```


- 당연히 Paging도 가능함.


```java
    @Test
    void findPageByName() {
        initTestData();

        Page<Parent> page = parentRepository.findPageByName("parent2", PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
```


## MongoRepository

위에서는 CrudRepository를 사용했지만, 보다 다양한 기능이 있는 Repository를 상속받는 것이 좋음.
예를 들어 `CrudRepository`말고 `PagingAndSortingRepository`를 상속받으면, `CrudRepository`의 기능을 전부 사용할 수 있으며, 추가로 정렬과 페이지네이션과 관련된 메서드까지 자동으로 생성해줌.

MongoDB를 사용하는 경우에는 `PagingAndSortingRepository` 말고 `MongoRepository`를 상속받는 것이 더 많은 기능을 사용할 수 있음. 

- findAll()과 같은 메서드의 반환타입이 List<>로 변경됨.
- `Query by Example` 기술을 사용할 수 있음.
- `count`, `exist`와 같은 메서드를 기본적으로 제공함.


```java
public interface ParentMongoRepository extends MongoRepository<Parent, String> {
}

public interface ChildMongoRepository extends MongoRepository<Child, String> {
}

@SpringBootTest
class ParentMongoRepositoryTest {
    // member variable omit..

    @Test
    void findAllPaging() {
        initTestData();         //default data is injected to Parent, Child collection 

        Page<Parent> parents = parentMongoRepository.findAll(PageRequest.of(0, 10));

        assertEquals(3, parents.getTotalElements());
        assertTrue(parents.isFirst());
    }
}
```

### 메서드 반환타입과 이름에 따른 차이

- `List<Person> findByLastname(String lastname)` : lastname에 해당하는 모든 Person을 조회함.

- `Page<Person> findByFirstname(String firstname, Pageable pageable)` : firstname에 해당하는 Persion을 page요청에 맞게 조회함.

- `Person findByShippingAddresses(Address address)` : 조회대상으로 Value Object를 사용할 수 있음. 리턴 타입을 단일 객체로 할 경우, 2개 이상이 조회되면 `IncorrectResultSizeDataAccessException` 예외 발생.

- `Person findFirstByLastname(String lastname)` : `findFirst`는 가장 위에 조회되는 한 건만 찾음.

- `Stream<Person> findAllBy()` : 자바8 이상부터는 반환타입을 Stream으로 받을 수도 있음.


### 삭제 쿼리

- `List <Person> deleteByLastname(String lastname)` : 삭제하기 전에 해당하는 데이터를 조회해서 반환함.

- `Long deletePersonByLastname(String lastname)` : 삭제하고 삭제된 데이터 수를 반환함.

- `Person deleteSingleByLastname(String lastname)` : 일치하는 첫 번째 document만 반환하고 삭제함.

- `Optional<Person> deleteByBirthdate(Date birthdate)` : NPE를 방지하기 위해 Optional을 썼을 뿐, 위와 동일함.


## QueryDSL 사용하기

### 의존성 주입

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>

<!-- Query DSL-->
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-core</artifactId>
</dependency>
```

```xml
<build>
    <plugins>
    ...
        <plugin>
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <executions>
                <execution>
                    <id>mongodb-processor</id>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>target/generated-sources/java</outputDirectory>
                        <processor>org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor</processor>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    ...
    </plugins>
</build>
```

- @Document 애노테이션을 사용하는 경우에는 `org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor`를 사용하고, Morphia Annotation을 사용하는 경우에는 `com.mysema.query.mongodb.MongodbAnnotationProcessor`로 processor를 변경한다.

- 의존성과 플러그인을 넣어줬으면, `mvn clean install`을 수행한다.

여기서는 Spring Data가 제공해주는 @Document 애노테이션을 사용해서 예제를 진행해본다.


### Repository 생성

```java
public interface ParentQueryDslRepository extends MongoRepository<Parent, String>, QuerydslPredicateExecutor<Parent> {
}
```

- `MongoRepository`와 `QuerydslPredicateExecutor`를 상속받는 레파지토리를 생성한다.

- `QuerydslPredicateExecutor` 안에는 `Predicate`로 데이터를 조회할 수 있는 메서드들이 정의되어있다. `Predicate`는 조회하고자 하는 대상을 Filtering하는 객체정도로 생각해도 무방할 것 같다.


### Predicate를 이용해 조회해보기.


```java

@Service
@RequiredArgsConstructor
public class ParentQueryDslService {
    @Autowired
    private final ParentQueryDslRepository parentRepository;

    public List<Parent> findParentByQueryDsl(String parentName, String childName) {
        QParent parent = QParent.parent;

        BooleanExpression predicate = parent.name.eq(parentName)
                                                 .and(parent.child.name.startsWith(childName));

        Iterable<Parent> all = parentRepository.findAll(predicate);

        List<Parent> parents = new ArrayList<>();
        all.forEach(parents::add);

        return parents;
    }
}
```

- `QClass`를 사용하는 것은 JPA QueryDSL을 사용할 때와 동일하다. JPA QueryDSL은 `JPAQueryFactory`에서 `Query`를 꺼내와 질의를 하는 구조로 되어있다. `MorphiaQuery`를 사용하면 거의 동일한 방식으로 데이터를 질의할 수 있다. 여기서는 `Predicate`를 이용해서 진행해보자.

- `parent.name.eq(parentName)`처럼 조건문을 `QClass`에서 바로 생성할 수 있고, 반환타입이 `BooleanExpression`이다. `BooleanExpression`은 `Predicate`를 구현한 객체로, 여러가지를 질의할 수 있는 편리한 기능을 제공한다.

