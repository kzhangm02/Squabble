import sys
import random
import copy
import pickle as pk
import math

def guess(words, candidates, pattern_knowledge, letter_knowledge, guess_num, pattern_dict):
   if len(candidates) == 1:
      return candidates[0]
   if len(pattern_knowledge) == 4:
      word1 = candidates[0]
      word2 = candidates[1]
      unk_idx = [k for k in range(len(word1)) if not word1[k] == word2[k]][0]
      freq_list = 'eariotnslcudpmhgbfywkvxzjq'
      for letter in freq_list:
         for candidate in candidates:
            if candidate[unk_idx] == letter:
               return candidate
   pattern_to_words = {}
   pattern_keys = set({})
   letter_to_words = {}
   letter_keys = set({})
   for candidate in candidates:
      patterns = pattern_dict[candidate]
      for pattern in patterns:
         if pattern in pattern_keys:
            pattern_to_words[pattern].append(candidate)
         else:
            pattern_to_words[pattern] = [candidate]
            pattern_keys.add(pattern)
      for letter in set(candidate):
         if letter in letter_keys:
            letter_to_words[letter].append(candidate)
         else:
            letter_to_words[letter] = [candidate]
            letter_keys.add(letter)
   pattern_weight = 0.1
   letter_weight_dict = {1: 1.0, 2: 0.9, 3: 0.67, 4: 0.41, 5: 0.2, 6: 0.83}
   letter_weight = letter_weight_dict[guess_num]
   best_word = ''
   best_score = -1
   explore_factor_dict = {1: 1.0, 2: 0.99, 3: 0.92, 4: 0.76, 5: 0.53, 6: 0.28}
   explore_factor = explore_factor_dict[guess_num]
   for word in words:
      score = 0
      patterns = pattern_dict[word]
      for pattern in patterns:
         if pattern in pattern_keys:
            if pattern in pattern_knowledge:
               score += (1 - explore_factor) * pattern_weight * len(pattern_to_words[pattern])
            else:
               score += explore_factor * pattern_weight * len(pattern_to_words[pattern])
      for letter in set(word):
         if letter in letter_keys:
            if letter in letter_knowledge.keys():
               score += (1 - explore_factor) * letter_weight * len(letter_to_words[letter])
            else:
               score += explore_factor * letter_weight * len(letter_to_words[letter])
      if score > best_score:
         best_score = score
         best_word = word
   return best_word

def remove_candidates(guess, candidates, result):
   remove_idx = set({})
   for n in range(len(candidates)):
      candidate = candidates[n]
      letter_dict = {c: candidate.count(c) for c in candidate}
      for k in range(len(guess)):
         if result[k] == '2':
            if not guess[k] == candidate[k]:
               remove_idx.add(n)
               break
            else:
               letter_dict[candidate[k]] -= 1
      if n not in remove_idx:
         for k in range(len(guess)):
            if result[k] == '1':
               if guess[k] == candidate[k]:
                  remove_idx.add(n)
                  break
               elif guess[k] not in letter_dict.keys():
                  remove_idx.add(n)
                  break
               elif letter_dict[guess[k]] == 0:
                  remove_idx.add(n)
                  break
               else:
                  letter_dict[guess[k]] -= 1
            elif result[k] == '0':
               if guess[k] in letter_dict.keys():
                  if letter_dict[guess[k]] > 0:
                     remove_idx.add(n)
                     break
   new_candidates = []
   for n in range(len(candidates)):
      if n not in remove_idx:
         new_candidates.append(candidates[n])
   return new_candidates

def add_to_knowledge(next_guess, result, pattern_knowledge, letter_knowledge):
   letter_dict = {}
   for k in range(len(next_guess)):
      letter_dict[next_guess[k]] = 0
   for k in range(len(next_guess)):
      if result[k] == '2':
         pattern = ['_'] * 5
         pattern[k] = next_guess[k]
         pattern = ''.join(pattern)
         pattern_knowledge.add(pattern)
         letter_dict[next_guess[k]] += 1
      elif result[k] == '1':
         letter_dict[next_guess[k]] += 1
   for k in range(len(next_guess)):
      if not result[k] == '0':
         letter_knowledge[next_guess[k]] = letter_dict[next_guess[k]]

def guess_solve(gr):
    with open('words.p', 'rb') as f:
       words = pk.load(f)
    f.close()
    with open('patterns.p', 'rb') as f:
       pattern_dict = pk.load(f)
    f.close()
    candidates = copy.copy(words)
    previous_guesses = []
    previous_results = []
    for num in range(len(gr) // 10):
        previous_guesses.append(gr[5 * num : 5 * (num + 1)])
        previous_results.append(gr[len(gr) // 2 + 5 * num : len(gr) // 2 + 5 * (num + 1)])
    while len(previous_guesses) < 5:
        previous_guesses.append('');
    while len(previous_results) < 5:
        previous_results.append('');
    pattern_knowledge = set({})
    letter_knowledge = {}
    guess_num = 0
    while (previous_guesses[guess_num] != ''):
        candidates = remove_candidates(previous_guesses[guess_num], candidates, list(previous_results[guess_num]))
        add_to_knowledge(previous_guesses[guess_num], previous_results[guess_num], pattern_knowledge, letter_knowledge)
        guess_num += 1
        if guess_num == 5:
            break
    next_guess = guess(words, candidates, pattern_knowledge, letter_knowledge, guess_num + 1, pattern_dict)
    return next_guess

if __name__ == "__main__":
    gr = sys.argv[1]
    if gr == 'start':
      gr = ''
    print(guess_solve(gr))