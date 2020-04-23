package com.chuncongcong.test;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chuncongcong.test.service.TestService;


@WebMvcTest
class TestApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TestService testService;

	@Test
	public void test() throws Exception {
		when(testService.test()).thenReturn("mock bean return");
		this.mockMvc.perform(get("/test")).andDo(print()).
				andExpect(content().string(containsString("mock bean return")));
	}


}
