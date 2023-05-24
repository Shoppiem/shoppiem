import {Chat} from "../../types";

export const loadHistory = (chatHistory: Chat[]) => {
  return chatHistory
  try {
    // TODO: load history from the server
    // @ts-ignore
    const _history: Chat[] = JSON.parse(localStorage.getItem("history"))
    return _history ? _history : []
  } catch (e) {
    console.log(e)
  }
  return []
}