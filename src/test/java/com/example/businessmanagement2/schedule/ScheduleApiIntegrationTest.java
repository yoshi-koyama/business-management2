package com.example.businessmanagement2.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@DataSet(value = "schedule/datasets/schedules.yml")
@AutoConfigureMockMvc
@DBRider
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ScheduleApiIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  @Transactional
  void 作業予定が全件取得に成功すると200で内容を返すこと() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/schedules"))
        .andExpect(MockMvcResultMatchers.status().is(200));
  }

  @Test
  @Transactional
  void 作業予定が全件取得できること() throws Exception {
    String response = mockMvc.perform(MockMvcRequestBuilders.get("/schedules"))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
            "results": [
                {
                    "id": 1,
                    "userid": 1,
                    "workingdate": "2022-12-06",
                    "place": "4階トイレ",
                    "workcontent": "墨出し",
                    "numberofpeople": 3
                },
                {
                    "id": 2,
                    "userid": 2,
                    "workingdate": "2022-12-07",
                    "place": "3階和室",
                    "workcontent": "配線",
                    "numberofpeople": 5
                },
                {
                    "id": 3,
                    "userid": 2,
                    "workingdate": "2022-12-10",
                    "place": "3階トイレ２",
                    "workcontent": "BOX取り付け",
                    "numberofpeople": 2
                },
                {
                    "id": 4,
                    "userid": 3,
                    "workingdate": "2022-12-09",
                    "place": "4階洋室",
                    "workcontent": "配管",
                    "numberofpeople": 4
                }
            ]
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 存在する作業予定のIDを指定したとき正常に作業予定が返されること() throws Exception {
    String response = mockMvc.perform(MockMvcRequestBuilders.get("/schedules/2"))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
            "id": 2,
            "userid": 2,
            "workingdate": "2022-12-07",
            "place": "3階和室",
            "workcontent": "配線",
            "numberofpeople": 5
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 存在しない作業予定のidにアクセスしたときにと404が返ること() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/schedules/99"))
        .andExpect(MockMvcResultMatchers.status().is(404));
  }

  @Test
  @Transactional
  void 検索時に該当するIDの作業予定がいないときエラーメッセージが返ること() throws Exception {
    String response = mockMvc.perform(MockMvcRequestBuilders.get("/schedules/99"))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
            "title": "Resource Not Found",
            "detail": "ScheduleEntity (id = 99) is not found."
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定の登録に成功すると201とレスポンスメッセージを返すこと() throws Exception {
    String response = mockMvc.perform(
            MockMvcRequestBuilders.post("/schedules").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "userid": 3,
                            "workingdate": "2023-12-30",
                            "place": "トイレ",
                            "workcontent": "墨出し",
                            "numberofpeople": "3"
                        }
                    """)).andExpect(MockMvcResultMatchers.status().is(201)).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
          "message": "作業予定を登録しました"
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  public void 作業予定の登録に成功するとLocationヘッダーの値が返ること() {
    UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("http")
        .host("localhost:8080").path("schedules/5").build().encode();

    assertEquals("http://localhost%3A8080/schedules/5", uriComponents.toUriString());
  }

  @Test
  @Transactional
  void 作業予定登録時過去の日付入力した場合エラーメッセージを返すこと() throws Exception {
    String response = mockMvc.perform(
            MockMvcRequestBuilders.post("/schedules").contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "userid": 3,
                                "workingdate": "2022-12-11",
                                "place": "トイレ",
                                "workcontent": "墨出し",
                                "numberofpeople": "3"
                            }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "workingdate",
                      "reason": "must be a date in the present or in the future"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定登録時文字数が256文字以上の場合エラーメッセージを返すこと() throws Exception {
    String response = mockMvc.perform(
            MockMvcRequestBuilders.post("/schedules").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 3,
                          "workingdate": "2023-12-30",
                          "place": "あいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあい",
                          "workcontent": "墨出し",
                          "numberofpeople": "3"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "place",
                      "reason": "size must be between 0 and 256"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定登録時空白がある場合エラーメッセージを返すこと() throws Exception {
    String response = mockMvc.perform(
            MockMvcRequestBuilders.post("/schedules").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 3,
                          "workingdate": "2023-12-30",
                          "place": "4階部屋",
                          "workcontent": "",
                          "numberofpeople": "3"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "workcontent",
                      "reason": "must not be blank"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定更新に成功すると200とレスポンスメッセージを返すこと() throws Exception {
    String response = mockMvc.perform(
            MockMvcRequestBuilders.patch("/schedules/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "id": 1,
                          "userid": 1,
                          "workingdate": "2023-12-30",
                          "place": "4階",
                          "workcontent": "墨出し",
                          "numberofpeople": "3"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().is(200)).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
        "message": "作業予定を更新しました"
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定更新時に該当するIDの作業予定がいないときエラーメッセージを返すこと() throws Exception {

    String response = mockMvc.perform(
            MockMvcRequestBuilders.patch("/schedules/99").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 1,
                          "workingdate": "2023-12-30",
                          "place": "4階",
                          "workcontent": "墨出し",
                          "numberofpeople": "3"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
        "title": "Resource Not Found",
        "detail": "ScheduleEntity (id = 99) is not found."
        }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定更新時文字数が256文字以上の場合エラーメッセージを返すこと() throws Exception {

    String response = mockMvc.perform(
            MockMvcRequestBuilders.patch("/schedules/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 1,
                          "workingdate": "2023-12-30",
                          "place": "4階",
                          "workcontent": "あいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあいうえおあい",
                          "numberofpeople": "4"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "workcontent",
                      "reason": "size must be between 0 and 256"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定更新時人数が０の場合エラーメッセージを返すこと() throws Exception {

    String response = mockMvc.perform(
            MockMvcRequestBuilders.patch("/schedules/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 1,
                          "workingdate": "2023-12-30",
                          "place": "4階",
                          "workcontent": "墨出し",
                          "numberofpeople": "0"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "numberofpeople",
                      "reason": "must be greater than or equal to 1"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 作業予定更新時人数が200以上の場合エラーメッセージを返すこと() throws Exception {

    String response = mockMvc.perform(
            MockMvcRequestBuilders.patch("/schedules/1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                          "userid": 1,
                          "workingdate": "2023-12-30",
                          "place": "4階",
                          "workcontent": "墨出し",
                          "numberofpeople": "201"
                      }
                    """)).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn()
        .getResponse().getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
          {
              "title": "Bad Request",
              "detail": "リクエストが不正です。正しいリクエストでリトライしてください",
              "invalidParams": [
                  {
                      "name": "numberofpeople",
                      "reason": "must be less than or equal to 200"
                  }
              ]
          }
        """, response, JSONCompareMode.STRICT);
  }

  @Test
  @Transactional
  void 指定した作業予定を1件削除できること() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.delete("/schedules/1"))
        .andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
  }

  @Test
  @Transactional
  void 削除時に該当するIDのユーザーがいないときエラーメッセージが返ること() throws Exception {
    String response = mockMvc.perform(MockMvcRequestBuilders.delete("/schedules/99"))
        .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn().getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals("""
        {
        "title": "Resource Not Found",
        "detail": "ScheduleEntity (id = 99) is not found."
        }
        """, response, JSONCompareMode.STRICT);
  }

}
