import { InMemoryCacheConfig, FieldMergeFunction } from '@apollo/client'

function concatLists(existing: any[] = [], incoming: any[]) {
  // Slicing is necessary because the existing data is
  // immutable, and frozen in development.
  // const merged = existing ? existing.slice(0) : [];
  // for (let i = 0; i < incoming.length; ++i) {
  //   merged[(args?.offset || 0) + i] = incoming[i];
  // }
  return [...existing, ...incoming];
};

const smartConcat: FieldMergeFunction<any, any> = (existing: any = [], incoming: any[], { args, readField }) => {
  if (existing.length === 0) return incoming
  const newList = []
  
  // interleave merging in desc order, like a merge sort
  for (let i = 0, j = 0; i < existing.length; ++i) {
    const exId = readField<number>('id', existing[i])
    // @ts-ignore
    for (let incId = readField<number>('id', incoming[j]); incId >= exId; ++j, incId = readField('id', incoming[j])) {
      if (incId !== exId) {
        newList.push(incoming[j])
      }
    }
    newList.push(existing[i])
  }
  

  return newList
}

const cacheSettings: InMemoryCacheConfig = {
  typePolicies: {
    Query: {
      fields: {
        placements: {
          keyArgs: [],
          merge: smartConcat,
        },
        players: { keyArgs: [], merge: concatLists },
        transactions: { keyArgs: [], merge: smartConcat },
        killedEntities: { keyArgs: [], merge: smartConcat },
        mobGrief: { keyArgs: [], merge: smartConcat },
      },
    },
  },
}

export default cacheSettings
