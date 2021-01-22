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
  const [inc0, incLast] = [incoming[0], incoming[incoming.length - 1]].map(d => readField('id', d))
  if (existing.length === 0) return incoming
  const [ex0, exLast] = [existing[0], existing[existing.length - 1]].map(d => readField('id', d))

  if (inc0 > ex0) {
    const sliceIdx = incoming.findIndex(d => readField('id', d) <= ex0)
    if (sliceIdx === -1) return [...incoming, ...existing]
    return incoming.slice(0, sliceIdx).concat(existing)
  }

  return [...existing, ...incoming];
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
      },
    },
  },
}

export default cacheSettings
